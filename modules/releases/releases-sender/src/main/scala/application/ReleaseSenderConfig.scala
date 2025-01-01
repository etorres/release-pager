package es.eriktorr.pager
package application

import cats.Show
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple4Semigroupal, showInterpolator}
import com.monovore.decline.Opts
import io.github.iltotore.iron.decline.given

final case class ReleaseSenderConfig(kafkaConfig: KafkaConfig, telegramConfig: TelegramConfig)

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

    val telegramConfig = Opts
      .env[TelegramConfig.ApiToken](
        name = "RELEASE_CHECKER_TELEGRAM_API_TOKEN",
        help = "Set Telegram API Token.",
      )
      .map(TelegramConfig.apply)

    (kafkaConfig, telegramConfig).mapN(ReleaseSenderConfig.apply)
