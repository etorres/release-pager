package es.eriktorr.pager
package db

import org.scalacheck.Gen

import java.time.LocalDateTime

object TestRepositoryGenerator:
  def repositoryRowGen(
      idGen: Gen[Long],
      groupIdGen: Gen[String],
      artifactIdGen: Gen[String],
      versionGen: Gen[String],
      updatedAtGen: Gen[LocalDateTime],
  ): Gen[RepositoryRow] = for
    id <- idGen
    groupId <- groupIdGen
    artifactId <- artifactIdGen
    version <- versionGen
    updatedAt <- updatedAtGen
  yield RepositoryRow(id, groupId, artifactId, version, updatedAt)
