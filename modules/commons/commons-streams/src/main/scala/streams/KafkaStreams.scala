package es.eriktorr.pager
package streams

import application.KafkaConfig
import commons.std.Compute

import cats.effect.IO
import cats.effect.kernel.Resource
import fs2.Stream
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.concurrent.duration.FiniteDuration

sealed abstract class KafkaStreams

object KafkaStreams:
  sealed trait Listener[T: Decoder] extends KafkaStreams:
    def listen(consumer: (String, T) => IO[Unit]): Stream[IO, Unit]

  sealed trait Sender[T: Encoder] extends KafkaStreams:
    def send(key: String, value: T): IO[Unit]

  final class KafkaListener[T: Decoder] private (
      kafkaConsumer: KafkaConsumer[IO, String, String],
      commitBatchSize: Int,
      commitBatchWindow: FiniteDuration,
  ) extends Listener[T]:
    override def listen(consumer: (String, T) => IO[Unit]): Stream[IO, Unit] = kafkaConsumer.stream
      .mapAsync(Compute.cores) { committable =>
        (for
          value <- IO.fromEither(for
            json <- parse(committable.record.value)
            value <- json.as[T]
          yield value)
          _ <- consumer(committable.record.key, value)
        yield ()).as(committable.offset)
      }
      .through(commitBatchWithin(commitBatchSize, commitBatchWindow))

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
        .map(kafkaConsumer =>
          KafkaListener[T](
            kafkaConsumer,
            kafkaConfig.commitBatch.size,
            kafkaConfig.commitBatch.window,
          ),
        )

  final class KafkaSender[T: Encoder] private (
      kafkaProducer: KafkaProducer[IO, String, String],
      topic: KafkaConfig.Topic,
  ) extends Sender[T]:
    override def send(key: String, message: T): IO[Unit] =
      val value = message.asJson.noSpaces
      val record = ProducerRecord(topic, key, value)
      kafkaProducer.produce(ProducerRecords.one(record)).flatten.as(CommitNow).void

  object KafkaSender:
    def resource[T](
        kafkaConfig: KafkaConfig,
    )(using encoder: Encoder[T]): Resource[IO, KafkaSender[T]] =
      KafkaProducer
        .resource(
          ProducerSettings[IO, String, String].withBootstrapServers(
            kafkaConfig.bootstrapServersAsString,
          ),
        )
        .map(kafkaProducer => KafkaSender[T](kafkaProducer, kafkaConfig.topic))
