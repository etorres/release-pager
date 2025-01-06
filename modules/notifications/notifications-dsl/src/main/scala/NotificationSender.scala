package es.eriktorr.pager

import cats.effect.IO

trait NotificationSender[Precondition, Notification, Notified]:
  def send(precondition: Precondition, notification: Notification): IO[Notified]
