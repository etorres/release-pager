package es.eriktorr.pager
package application

import cats.data.NonEmptyList
import cats.implicits.catsSyntaxEitherId
import com.comcast.ip4s.{host, port}
import com.monovore.decline.{Command, Help}
import munit.FunSuite

import scala.util.Properties

final class ReleaseSenderConfigSuite extends FunSuite:
  test("should load configuration from environment"):
    assume(Properties.envOrNone("SBT_TEST_ENV_VARS").nonEmpty, "this test runs only on sbt")
    assertEquals(
      Command(name = "name", header = "header")(ReleaseSenderConfig.opts)
        .parse(List.empty, sys.env),
      ReleaseSenderConfig(
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
      ).asRight[Help],
    )
