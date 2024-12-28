package es.eriktorr.pager
package streams

import application.KafkaTestConfig
import commons.std.TSIDGen
import streams.Streams.KafkaSender

import cats.effect.IO
import cats.effect.kernel.Resource
import io.circe.Encoder

final class KafkaTestSender(kafkaTestConfig: KafkaTestConfig):
  def resource[T: Encoder]: Resource[IO, KafkaSender[T]] = for
    tsidGen <- TSIDGen.resource[IO]
    given TSIDGen[IO] = tsidGen
    sender <- KafkaSender.resource[T](kafkaTestConfig.config)
  yield sender
