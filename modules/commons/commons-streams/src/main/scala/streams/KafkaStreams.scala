package es.eriktorr.pager
package streams

import application.KafkaConfig
import commons.std.{Compute, TSIDGen}

import cats.effect.IO
import cats.effect.kernel.Resource
import fs2.Stream
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.concurrent.duration.DurationInt

sealed abstract class KafkaStreams

object KafkaStreams:
  sealed trait Listener[T: Decoder] extends KafkaStreams:
    def listen(consumer: T => IO[Unit]): Stream[IO, Unit]

  sealed trait Sender[T: Encoder] extends KafkaStreams:
    def send(message: T): IO[Unit]

  final class KafkaListener[T: Decoder] private (
      kafkaConsumer: KafkaConsumer[IO, String, String],
      commitBatchSize: Int,
  ) extends Listener[T]:
    override def listen(consumer: T => IO[Unit]): Stream[IO, Unit] = kafkaConsumer.stream
      .mapAsync(Compute.cores) { committable =>
        (for
          value <- IO.fromEither(for
            json <- parse(committable.record.value)
            value <- json.as[T]
          yield value)
          _ <- consumer(value)
        yield ()).as(committable.offset)
      }
      .through(commitBatchWithin(commitBatchSize, 15.seconds))

  object KafkaListener:
    def resource[T](
        kafkaConfig: KafkaConfig,
    )(using decoder: Decoder[T]): Resource[IO, KafkaListener[T]] =
      KafkaConsumer
        .resource(
          ConsumerSettings[IO, String, String]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers(kafkaConfig.bootstrapServersAsString)
            .withGroupId(kafkaConfig.consumerGroup),
        )
        .evalTap(_.subscribeTo(kafkaConfig.topic))
        .map(kafkaConsumer => KafkaListener[T](kafkaConsumer, kafkaConfig.commitBatchSize))

  final class KafkaSender[T: Encoder] private (
      kafkaProducer: KafkaProducer[IO, String, String],
      topic: KafkaConfig.Topic,
  )(using tsidGen: TSIDGen[IO])
      extends Sender[T]:
    override def send(message: T): IO[Unit] = for
      tsid <- tsidGen.randomTSID
      producerRecord = ProducerRecord(
        topic,
        tsid.toString,
        message.asJson.noSpaces,
      )
      _ <- kafkaProducer.produceOne_(producerRecord).flatten.as(CommitNow)
    yield ()

  object KafkaSender:
    def resource[T](kafkaConfig: KafkaConfig)(using
        tsidGen: TSIDGen[IO],
        encoder: Encoder[T],
    ): Resource[IO, KafkaSender[T]] =
      KafkaProducer
        .resource(
          ProducerSettings[IO, String, String].withBootstrapServers(
            kafkaConfig.bootstrapServersAsString,
          ),
        )
        .map(kafkaProducer => KafkaSender[T](kafkaProducer, kafkaConfig.topic))
