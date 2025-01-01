package es.eriktorr.pager
package api

import cats.effect.IO
import org.http4s.client.Client

trait Broadcaster:
  def broadcast(notification: Notification): IO[Unit]

object Broadcaster:
  final class Slack(httpClient: Client[IO]):
    def broadcast(notification: Notification): IO[Unit] =
      assert(httpClient == httpClient) // TODO
      IO.unit
