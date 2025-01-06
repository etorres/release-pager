package es.eriktorr.pager

import streams.KafkaStreams.KafkaSender

import cats.effect.IO

object NotificationSenderImpl:
  final class Kafka(sender: KafkaSender[Notification])
      extends NotificationSender[Unit, Notification, Unit]:
    override def send(precondition: Unit, notification: Notification): IO[Unit] =
      sender.send(notification.projectName, notification)

    def send(notification: Notification): IO[Unit] = send((), notification)
