package es.eriktorr.pager
package spec

import application.JdbcTestConfig
import db.PostgresTestTransactor

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Test

trait PostgresSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  val testTransactor: PostgresTestTransactor = PostgresTestTransactor(JdbcTestConfig.LocalContainer)
