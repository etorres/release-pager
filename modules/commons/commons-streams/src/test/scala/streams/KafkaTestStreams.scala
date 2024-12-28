package es.eriktorr.pager
package streams

import application.KafkaTestConfig
import commons.std.TSIDGen
import streams.Streams.{KafkaListener, KafkaSender}

import cats.effect.{IO, Resource}
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import io.circe.{Decoder, Encoder}

final class KafkaTestStreams(kafkaTestConfig: KafkaTestConfig):
  def resource[T: Encoder: Decoder]: Resource[IO, (KafkaSender[T], KafkaListener[T])] = for
    adminClient <- KafkaAdminClient.resource[IO](
      AdminClientSettings(kafkaTestConfig.config.bootstrapServersAsString),
    )
    _ <- Resource.eval(adminClient.deleteTopic(kafkaTestConfig.config.topic))
    tsidGen <- TSIDGen.resource[IO]
    given TSIDGen[IO] = tsidGen
    sender <- KafkaSender.resource[T](kafkaTestConfig.config)
    listener <- KafkaListener.resource[T](kafkaTestConfig.config)
  yield sender -> listener
