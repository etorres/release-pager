package es.eriktorr.pager
package application

import scala.concurrent.duration.DurationInt

enum KafkaTestConfig(val config: KafkaConfig):
  case LocalContainer
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaConfig.defaultBootstrapServers,
          KafkaConfig.defaultConsumerGroup,
          KafkaConfig.defaultTopic,
          KafkaConfig.CommitBatch(KafkaConfig.CommitBatchSize.applyUnsafe(2), 2.seconds),
        ),
      )
