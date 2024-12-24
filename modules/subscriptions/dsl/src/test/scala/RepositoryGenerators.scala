package es.eriktorr.pager

import Repository.{ArtifactId, GroupId, Version}
import commons.spec.StringGenerators.alphaLowerStringBetween

import org.scalacheck.Gen

object RepositoryGenerators:
  val artifactIdGen: Gen[ArtifactId] = alphaLowerStringBetween(3, 7).map(ArtifactId.applyUnsafe)

  val groupIdGen: Gen[GroupId] = alphaLowerStringBetween(3, 7).map(GroupId.applyUnsafe)

  val versionGen: Gen[Version] = alphaLowerStringBetween(3, 7).map(Version.applyUnsafe)

  def projectGen(
      groupIdGen: Gen[GroupId] = groupIdGen,
      artifactIdGen: Gen[ArtifactId] = artifactIdGen,
  ): Gen[(GroupId, ArtifactId)] = for
    groupId <- groupIdGen
    artifactId <- artifactIdGen
  yield groupId -> artifactId
