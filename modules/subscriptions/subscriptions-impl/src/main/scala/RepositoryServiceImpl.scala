package es.eriktorr.pager

import db.RepositoryMapper

import cats.effect.IO
import doobie.Fragment
import doobie.hikari.HikariTransactor
import doobie.implicits.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RepositoryServiceImpl:
  final class Postgres(
      transactor: HikariTransactor[IO],
      config: RepositoryServiceConfig = RepositoryServiceConfig.default,
  ) extends RepositoryService[Repository, Unit, Repository.Version]
      with RepositoryMapper:
    override def findEarliestUpdates(): IO[List[Repository]] =
      val frequencyFragment = Fragment.const(s"'${config.frequency.toHours} hours'")
      val sql = sql"""SELECT
                     |  id, group_id, artifact_id, version 
                     |FROM repositories 
                     |WHERE updated_at + interval $frequencyFragment <= CURRENT_TIMESTAMP
                     |LIMIT ${config.limit}""".stripMargin
      for repositories <- sql
          .query[Repository]
          .to[List]
          .transact(transactor)
      yield repositories

    override def update(repository: Repository, version: Repository.Version): IO[Unit] =
      val sql = sql"""UPDATE repositories
                     |SET version = $version
                     |WHERE id = ${repository.id}""".stripMargin
      sql.update.run.transact(transactor).void

  final case class RepositoryServiceConfig(frequency: FiniteDuration, limit: Int)

  object RepositoryServiceConfig:
    val default: RepositoryServiceConfig = RepositoryServiceConfig(1.day, 100)
