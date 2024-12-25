package es.eriktorr.pager
package db

import commons.spec.TemporalGenerators

import org.scalacheck.Gen

import java.time.LocalDateTime

object TestRepositoryGenerator:
  def repositoryRowGen(
      idGen: Gen[Long] = SubscriptionGenerator.idGen.map(_.toLong),
      groupIdGen: Gen[String] = RepositoryGenerators.groupIdGen,
      artifactIdGen: Gen[String] = RepositoryGenerators.artifactIdGen,
      versionGen: Gen[String] = RepositoryGenerators.versionGen,
      updatedAtGen: Gen[LocalDateTime] = TemporalGenerators.localDateTimeGen,
  ): Gen[RepositoryRow] = for
    id <- idGen
    groupId <- groupIdGen
    artifactId <- artifactIdGen
    version <- versionGen
    updatedAt <- updatedAtGen
  yield RepositoryRow(id, groupId, artifactId, version, updatedAt)
