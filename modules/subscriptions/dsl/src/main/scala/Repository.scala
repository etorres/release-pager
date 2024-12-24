package es.eriktorr.pager

import commons.refined.Constraints.NonEmptyString

import cats.effect.IO
import cats.implicits.catsSyntaxEither
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.Positive0
import io.hypersistence.tsid.TSID

final case class Repository(
    id: Repository.Id,
    groupId: Repository.GroupId,
    artifactId: Repository.ArtifactId,
    version: Repository.Version,
):
  def projectName: String = s"$groupId:$artifactId"

object Repository:
  opaque type Id <: Long :| Positive0 = Long :| Positive0

  object Id extends RefinedTypeOps[Long, Positive0, Id]:
    def io(tsid: TSID): IO[Id] =
      IO.fromEither(Id.either(tsid.toLong.nn).leftMap(IllegalArgumentException(_)))

  opaque type GroupId <: String :| NonEmptyString = String :| NonEmptyString

  object GroupId extends RefinedTypeOps[String, NonEmptyString, GroupId]

  opaque type ArtifactId <: String :| NonEmptyString = String :| NonEmptyString

  object ArtifactId extends RefinedTypeOps[String, NonEmptyString, ArtifactId]

  opaque type Version <: String :| NonEmptyString = String :| NonEmptyString

  object Version extends RefinedTypeOps[String, NonEmptyString, Version]
