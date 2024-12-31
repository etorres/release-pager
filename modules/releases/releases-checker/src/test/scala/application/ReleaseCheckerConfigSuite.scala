package es.eriktorr.pager
package application

import commons.Secret

import cats.collections.Range
import cats.data.NonEmptyList
import cats.implicits.catsSyntaxEitherId
import com.comcast.ip4s.{host, port}
import com.github.eikek.calev.CalEvent
import com.monovore.decline.{Command, Help}
import munit.FunSuite

import scala.concurrent.duration.DurationInt
import scala.util.Properties

final class ReleaseCheckerConfigSuite extends FunSuite:
  test("should load configuration from environment"):
    assume(Properties.envOrNone("SBT_TEST_ENV_VARS").nonEmpty, "this test runs only on sbt")
    assertEquals(
      Command(name = "name", header = "header")(ReleaseCheckerConfig.opts)
        .parse(List.empty, sys.env),
      ReleaseCheckerConfig(
        JdbcConfig.postgresql(
          Range(2, 4),
          JdbcConfig.ConnectUrl.applyUnsafe("jdbc:postgresql://localhost:5432/release_pager"),
          Secret(JdbcConfig.Password.applyUnsafe("release_pager_password")),
          JdbcConfig.Username.applyUnsafe("release_pager_username"),
        ),
        KafkaConfig(
          NonEmptyList.of(
            KafkaConfig.BootstrapServer(host"kafka.test", port"29092"),
            KafkaConfig.BootstrapServer(host"kafka.test", port"39092"),
            KafkaConfig.BootstrapServer(host"kafka.test", port"49092"),
          ),
          KafkaConfig.ConsumerGroup.applyUnsafe("kafka_consumer_group"),
          KafkaConfig.Topic.applyUnsafe("kafka_topic"),
          KafkaConfig.defaultCommitBatch,
        ),
        SchedulerConfig(
          CalEvent.unsafe("*-*-* *:0/20:00"),
          13.minutes,
        ),
      ).asRight[Help],
    )
