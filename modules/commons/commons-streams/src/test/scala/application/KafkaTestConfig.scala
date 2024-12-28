package es.eriktorr.pager
package application

import cats.data.NonEmptyList

enum KafkaTestConfig(val config: KafkaConfig):
  case LocalContainer
      extends KafkaTestConfig(
        KafkaConfig(
          NonEmptyList.one(KafkaConfig.defaultBootstrapServer),
          KafkaConfig.defaultConsumerGroup,
          KafkaConfig.defaultTopic,
          KafkaConfig.CommitBatchSize.applyUnsafe(1),
        ),
      )
