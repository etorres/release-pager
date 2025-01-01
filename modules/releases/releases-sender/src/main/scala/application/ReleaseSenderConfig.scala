package es.eriktorr.pager
package application

import cats.Show
import cats.implicits.{catsSyntaxTuple4Semigroupal, showInterpolator}
import com.monovore.decline.Opts
import io.github.iltotore.iron.decline.given

final case class ReleaseSenderConfig(kafkaConfig: KafkaConfig)

object ReleaseSenderConfig:
  given Show[ReleaseSenderConfig] = Show.show(config => show"[${config.kafkaConfig}")

  def opts: Opts[ReleaseSenderConfig] =
    val kafkaConfig = (
      Opts
        .env[String](
          name = "RELEASE_CHECKER_KAFKA_BOOTSTRAP_SERVERS",
          help = "Set Kafka Bootstrap Servers.",
        )
        .withDefault(KafkaConfig.defaultBootstrapServers.map(_.asString).toList.mkString(","))
        .map(KafkaConfig.BootstrapServer.from),
      Opts
        .env[KafkaConfig.ConsumerGroup](
          name = "RELEASE_CHECKER_KAFKA_CONSUMER_GROUP",
          help = "Set Kafka Consumer Group.",
        )
        .withDefault(KafkaConfig.defaultConsumerGroup),
      Opts
        .env[KafkaConfig.Topic](
          name = "RELEASE_CHECKER_KAFKA_TOPIC",
          help = "Set Kafka Topic.",
        )
        .withDefault(KafkaConfig.defaultTopic),
      Opts(KafkaConfig.defaultCommitBatch),
    ).mapN(KafkaConfig.apply)

    kafkaConfig.map(ReleaseSenderConfig.apply)
