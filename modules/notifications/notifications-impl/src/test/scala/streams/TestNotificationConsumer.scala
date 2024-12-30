package es.eriktorr.pager
package streams

import streams.TestNotificationConsumer.NotificationConsumerState

import cats.effect.{IO, Ref}

final class TestNotificationConsumer(stateRef: Ref[IO, NotificationConsumerState]):
  def consume(notification: Notification): IO[Unit] = stateRef.update: currentState =>
    currentState.copy(notification :: currentState.notifications)

object TestNotificationConsumer:
  final case class NotificationConsumerState(notifications: List[Notification])

  object NotificationConsumerState:
    val empty: NotificationConsumerState = NotificationConsumerState(List.empty)
