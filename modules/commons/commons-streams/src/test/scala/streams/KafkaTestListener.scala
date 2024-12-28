package es.eriktorr.pager
package streams

import application.KafkaTestConfig
import streams.Streams.KafkaListener

import cats.effect.IO
import cats.effect.kernel.Resource
import io.circe.Decoder

final class KafkaTestListener(kafkaTestConfig: KafkaTestConfig):
  def resource[T: Decoder]: Resource[IO, KafkaListener[T]] =
    KafkaListener.resource[T](kafkaTestConfig.config)
