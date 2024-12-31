package es.eriktorr.pager
package application

import application.argument.SchedulerConfigArgument.given
import commons.Secret
import commons.argument.RangeArgument.given

import cats.Show
import cats.collections.Range
import cats.data.NonEmptyList
import cats.implicits.{
  catsSyntaxTuple2Semigroupal,
  catsSyntaxTuple3Semigroupal,
  catsSyntaxTuple4Semigroupal,
  showInterpolator,
}
import com.github.eikek.calev.CalEvent
import com.monovore.decline.Opts
import io.github.iltotore.iron.decline.given

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class ReleaseCheckerConfig(
    jdbcConfig: JdbcConfig,
    kafkaConfig: KafkaConfig,
    schedulerConfig: SchedulerConfig,
)

object ReleaseCheckerConfig:
  given Show[ReleaseCheckerConfig] =
    import scala.language.unsafeNulls
    Show.show(config => show"""[${config.jdbcConfig},
                              | ${config.kafkaConfig}
                              | ${config.schedulerConfig}]""".stripMargin.replaceAll("\\R", ""))

  def opts: Opts[ReleaseCheckerConfig] =
    val jdbcConfig = (
      Opts
        .env[Range[Int]](
          name = "RELEASE_CHECKER_JDBC_CONNECTIONS",
          help = "Set JDBC Connections.",
        )
        .validate("Must be between 1 and 16")(_.overlaps(Range(1, 16)))
        .withDefault(Range(1, 3)),
      Opts.env[JdbcConfig.ConnectUrl](
        name = "RELEASE_CHECKER_JDBC_CONNECT_URL",
        help = "Set JDBC Connect URL.",
      ),
      Opts
        .env[JdbcConfig.Password](
          name = "RELEASE_CHECKER_JDBC_PASSWORD",
          help = "Set JDBC Password.",
        )
        .map(Secret.apply[JdbcConfig.Password]),
      Opts.env[JdbcConfig.Username](
        name = "RELEASE_CHECKER_JDBC_USERNAME",
        help = "Set JDBC Username.",
      ),
    ).mapN(JdbcConfig.postgresql)

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

    val schedulerConfig = (
      Opts
        .env[CalEvent](
          name = "RELEASE_CHECKER_SCHEDULER_CALENDAR_EVENT",
          help = "Set scheduler trigger.",
        )
        .withDefault(CalEvent.unsafe("*-*-* *:15:00")),
      Opts
        .env[FiniteDuration](
          name = "RELEASE_CHECKER_SCHEDULER_CHECK_FREQUENCY",
          help = "Set scheduler check frequency.",
        )
        .withDefault(1.hours),
    ).mapN(SchedulerConfig.apply)

    (jdbcConfig, kafkaConfig, schedulerConfig).mapN(ReleaseCheckerConfig.apply)
