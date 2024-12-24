package es.eriktorr.pager
package db

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.Update
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDateTime

final class TestRepositoryServiceImpl(transactor: HikariTransactor[IO]):
  def addAll(repositories: NonEmptyList[RepositoryRow]): IO[Unit] =
    val sql = """INSERT INTO repositories
                |(id, group_id, artifact_id, version, updated_at) 
                |values (?, ?, ?, ?, ?)""".stripMargin
    Update[RepositoryRow](sql)
      .updateMany(repositories)
      .transact(transactor)
      .void
