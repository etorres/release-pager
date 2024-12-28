package es.eriktorr.pager

import streams.Streams.KafkaListener

import cats.effect.IO
import fs2.Stream

object NotificationListenerImpl:
  final class Kafka(listener: KafkaListener[Notification]) extends NotificationListener:
    def stream: Stream[IO, Unit] =
      listener.listen(notification => IO.println(s" >> Received: $notification")) // TODO
