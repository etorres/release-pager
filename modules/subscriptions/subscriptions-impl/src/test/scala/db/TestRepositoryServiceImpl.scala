package es.eriktorr.pager
package db

import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import doobie.Update
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDateTime

final class TestRepositoryServiceImpl(transactor: HikariTransactor[IO]) extends RepositoryMapper:
  def addAll(row: NonEmptyList[RepositoryRow]): IO[Unit] =
    val sql = """INSERT INTO repositories
                |(id, group_id, artifact_id, version, updated_at) 
                |values (?, ?, ?, ?, ?)""".stripMargin
    Update[RepositoryRow](sql)
      .updateMany(row)
      .transact(transactor)
      .void

  def findBy(id: Repository.Id): OptionT[IO, Repository] =
    val sql = sql"""SELECT
                   | id, group_id, artifact_id, version
                   |FROM repositories
                   |WHERE id = $id""".stripMargin
    OptionT(sql.query[Repository].option.transact(transactor))
