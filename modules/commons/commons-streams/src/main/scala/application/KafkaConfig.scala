package es.eriktorr.pager
package application

import commons.refined.Constraints.{Between, NonEmptyString}

import cats.Show
import cats.data.NonEmptyList
import com.comcast.ip4s.{host, port, Host, Port}
import io.github.iltotore.iron.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class KafkaConfig(
    bootstrapServers: NonEmptyList[KafkaConfig.BootstrapServer],
    consumerGroup: KafkaConfig.ConsumerGroup,
    topic: KafkaConfig.Topic,
    commitBatch: KafkaConfig.CommitBatch,
):
  def bootstrapServersAsString: String = bootstrapServers.map(_.asString).toList.mkString(",")

object KafkaConfig:
  final case class BootstrapServer(host: Host, port: Port):
    def asString: String = s"${host.toString}:${port.value}"

  object BootstrapServer:
    def from(value: String): NonEmptyList[BootstrapServer] =
      import scala.language.unsafeNulls
      NonEmptyList.fromListUnsafe(
        value
          .split(",")
          .toList
          .map { server =>
            val serverParts = server.split(":", 1).toList
            serverParts match
              case ::(head, next) =>
                for
                  host <- Host.fromString(head)
                  port <- next.lastOption.flatMap(Port.fromString)
                yield BootstrapServer(host, port)
              case Nil => None
          }
          .collect { case Some(bootstrapServer) => bootstrapServer },
      )

  opaque type ConsumerGroup <: String :| NonEmptyString = String :| NonEmptyString

  object ConsumerGroup extends RefinedTypeOps[String, NonEmptyString, ConsumerGroup]

  opaque type Topic <: String :| NonEmptyString = String :| NonEmptyString

  object Topic extends RefinedTypeOps[String, NonEmptyString, Topic]

  private type ValidCommitBatchSize = Between[1, 1000]

  opaque type CommitBatchSize <: Int :| ValidCommitBatchSize = Int :| ValidCommitBatchSize

  object CommitBatchSize extends RefinedTypeOps[Int, ValidCommitBatchSize, CommitBatchSize]

  final case class CommitBatch(size: CommitBatchSize, window: FiniteDuration)

  val defaultBootstrapServers: NonEmptyList[BootstrapServer] =
    NonEmptyList.one(BootstrapServer(host"localhost", port"9092"))
  val defaultCommitBatch: CommitBatch = CommitBatch(CommitBatchSize(500), 15.seconds)
  val defaultConsumerGroup: ConsumerGroup = ConsumerGroup("release-pager")
  val defaultTopic: Topic = Topic("repository-releases")

  given Show[KafkaConfig] =
    import scala.language.unsafeNulls
    Show.show(config =>
      s"""kafka-bootstrap-servers: ${config.bootstrapServersAsString},
         | kafka-commit-batch: ${config.commitBatch.size} or ${config.commitBatch.window.toSeconds}s,
         | kafka-consumer-group: ${config.consumerGroup},
         | kafka-topic: ${config.topic}""".stripMargin.replaceAll("\\R", ""),
    )
