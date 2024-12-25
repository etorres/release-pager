package es.eriktorr.pager

import cats.effect.IO

object FakeNotificationBuilder extends NotificationBuilder[String, Int, String, String]:
  override def make(subscribers: List[String], repository: Int, version: String): IO[String] =
    IO.pure(
      s"notification->{subscribers:${subscribers.sorted.mkString("[", ",", "]")},repository:$repository,version:$version}",
    )
