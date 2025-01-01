package es.eriktorr.pager

import cats.effect.IO
import fs2.Stream

final class ReleaseSender(notificationListener: NotificationListener):
  def stream: Stream[IO, Unit] = notificationListener.stream

object ReleaseSender:
  final class Default
