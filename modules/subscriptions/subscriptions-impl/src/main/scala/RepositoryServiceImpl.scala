package es.eriktorr.pager

import Repository.Version

import cats.effect.IO
import cats.implicits.{catsSyntaxEither, catsSyntaxTuple4Parallel, toTraverseOps}
import doobie.Fragment
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import io.github.iltotore.iron.cats.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RepositoryServiceImpl:
  final class Postgres(
      transactor: HikariTransactor[IO],
      config: RepositoryServiceConfig = RepositoryServiceConfig.default,
  ) extends RepositoryService[Repository, Unit, Repository.Version]:
    override def findEarliestUpdates(): IO[List[Repository]] =
      val frequencyFragment = Fragment.const(s"'${config.frequency.toHours} hours'")
      val sql = sql"""SELECT
                     |  id, group_id, artifact_id, version 
                     |FROM repositories 
                     |WHERE updated_at + interval $frequencyFragment <= CURRENT_TIMESTAMP
                     |LIMIT ${config.limit}""".stripMargin
      for
        rows <- sql
          .query[(Long, String, String, String)]
          .to[List]
          .transact(transactor)
        repositories <- IO.fromEither(rows.traverse { case (id, groupId, artifactId, version) =>
          (
            Repository.Id.eitherNec(id),
            Repository.GroupId.eitherNec(groupId),
            Repository.ArtifactId.eitherNec(artifactId),
            Repository.Version.eitherNec(version),
          ).parMapN(Repository.apply)
            .leftMap(errors => IllegalArgumentException(errors.reduceLeft(_ + ", " + _)))
        })
      yield repositories

    override def update(repository: Repository, version: Version): IO[Unit] = IO.unit // TODO

  final case class RepositoryServiceConfig(frequency: FiniteDuration, limit: Int)

  object RepositoryServiceConfig:
    val default: RepositoryServiceConfig = RepositoryServiceConfig(1.day, 100)