package es.eriktorr.pager
package db

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.Update
import doobie.hikari.HikariTransactor
import doobie.implicits.*

final class TestSubscriptionServiceImpl(transactor: HikariTransactor[IO]):
  def addAll(row: NonEmptyList[SubscriberRow]): IO[Unit] =
    val sql = """INSERT INTO subscribers
                |(id, chat_id, name)
                |values (?, ?, ?)""".stripMargin
    Update[SubscriberRow](sql)
      .updateMany(row)
      .transact(transactor)
      .void
