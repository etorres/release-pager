package es.eriktorr.pager

import cats.effect.IO

object FakeNotificationBuilder extends NotificationBuilder[String, String, String]:
  override def make(subscriptions: List[String], version: String): IO[String] =
    IO.pure(
      s"notification->{subscriptions:${subscriptions.sorted.mkString("[", ",", "]")},version:$version}",
    )
