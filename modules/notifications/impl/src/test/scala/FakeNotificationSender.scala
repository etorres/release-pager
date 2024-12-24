package es.eriktorr.pager

import cats.effect.IO

object FakeNotificationSender extends NotificationSender[String, String, String]:
  override def send(precondition: String, notification: String): IO[String] =
    IO.pure(s"notified->{$precondition,$notification}")
