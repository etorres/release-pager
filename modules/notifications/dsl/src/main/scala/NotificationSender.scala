package es.eriktorr.pager

import cats.effect.IO

trait NotificationSender[Notification, Notified, Precondition]:
  def send(precondition: Precondition, notification: Notification): IO[Notified]
