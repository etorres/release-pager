package es.eriktorr.pager

import commons.spec.StringGenerators.alphaLowerStringBetween

import org.scalacheck.Gen

object RepositoryGenerators:
  val idGen: Gen[Long] = Gen.choose(1L, Long.MaxValue)

  def projectGen(
      groupIdGen: Gen[String] = alphaLowerStringBetween(3, 7),
      artifactIdGen: Gen[String] = alphaLowerStringBetween(3, 7),
  ): Gen[(String, String)] = for
    groupId <- groupIdGen
    artifactId <- artifactIdGen
  yield groupId -> artifactId
