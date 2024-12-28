package es.eriktorr.pager
package spec

import application.KafkaTestConfig
import streams.KafkaTestStreams

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Test

trait KafkaSuite[T] extends CatsEffectSuite with ScalaCheckEffectSuite:
  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  val testStreams: KafkaTestStreams = KafkaTestStreams(KafkaTestConfig.LocalContainer)
