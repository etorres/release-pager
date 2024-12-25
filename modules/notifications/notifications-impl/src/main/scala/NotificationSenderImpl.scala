package es.eriktorr.pager

import cats.effect.IO

object NotificationSenderImpl:
  final class Redis extends NotificationSender[Notification, Unit, Unit]:
    override def send(precondition: Unit, notification: Notification): IO[Unit] = IO.unit // TODO
