package es.eriktorr.pager
package application

import commons.Secret
import commons.argument.RangeArgument.given

import cats.Show
import cats.collections.Range
import cats.data.NonEmptyList
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple4Semigroupal, showInterpolator}
import com.monovore.decline.Opts
import io.github.iltotore.iron.decline.given

final case class ReleaseCheckerConfig(kafkaConfig: KafkaConfig, jdbcConfig: JdbcConfig)

object ReleaseCheckerConfig:
  given Show[ReleaseCheckerConfig] =
    import scala.language.unsafeNulls
    Show.show(config => show"""[${config.kafkaConfig},
                              | ${config.jdbcConfig}]""".stripMargin.replaceAll("\\R", ""))

  def opts: Opts[ReleaseCheckerConfig] =
    val kafkaConfig = (
      Opts
        .env[String](
          name = "RELEASE_CHECKER_KAFKA_BOOTSTRAP_SERVERS",
          help = "Set Kafka Bootstrap Servers.",
        )
        .map(KafkaConfig.BootstrapServer.from)
        .withDefault(NonEmptyList.one(KafkaConfig.defaultBootstrapServer)),
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
      Opts
        .env[KafkaConfig.CommitBatchSize](
          name = "RELEASE_CHECKER_KAFKA_COMMIT_BATCH_SIZE",
          help = "Set Kafka Commit Batch Size.",
        )
        .withDefault(KafkaConfig.defaultCommitBatchSize),
    ).mapN(KafkaConfig.apply)

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

    (kafkaConfig, jdbcConfig).mapN(ReleaseCheckerConfig.apply)
