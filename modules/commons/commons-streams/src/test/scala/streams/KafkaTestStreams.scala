package es.eriktorr.pager
package streams

import application.KafkaTestConfig
import commons.std.TSIDGen
import streams.KafkaStreams.{KafkaListener, KafkaSender}

import cats.effect.{IO, Resource}
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException

final class KafkaTestStreams(kafkaTestConfig: KafkaTestConfig):
  def resource[T: Encoder: Decoder]: Resource[IO, (KafkaSender[T], KafkaListener[T])] = for
    _ <- recreateTopics(kafkaTestConfig)
    tsidGen <- TSIDGen.resource[IO]
    given TSIDGen[IO] = tsidGen
    sender <- KafkaSender.resource[T](kafkaTestConfig.config)
    listener <- KafkaListener.resource[T](kafkaTestConfig.config)
  yield sender -> listener

  private def recreateTopics(kafkaTestConfig: KafkaTestConfig) =
    for
      adminClient <- KafkaAdminClient.resource[IO](
        AdminClientSettings(kafkaTestConfig.config.bootstrapServersAsString),
      )
      _ <- Resource.eval(adminClient.deleteTopic(kafkaTestConfig.config.topic).recoverWith {
        case _: UnknownTopicOrPartitionException => IO.unit
      })
      _ <- Resource.eval(
        adminClient.createTopic(NewTopic(kafkaTestConfig.config.topic, 1, 1.toShort)),
      )
    yield ()
