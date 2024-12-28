package es.eriktorr.pager

import cats.effect.IO
import fs2.Stream

trait NotificationListener:
  def stream: Stream[IO, Unit]
