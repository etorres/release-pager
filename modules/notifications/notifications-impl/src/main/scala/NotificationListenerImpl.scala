package es.eriktorr.pager

import streams.KafkaStreams.KafkaListener

import cats.effect.IO
import fs2.Stream

object NotificationListenerImpl:
  final class Kafka(listener: KafkaListener[Notification], consumer: Notification => IO[Unit])
      extends NotificationListener:
    def stream: Stream[IO, Unit] =
      listener.listen:
        case (_, notification) => consumer(notification)
