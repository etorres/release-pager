package es.eriktorr.pager
package streams

import streams.Streams.KafkaListener

import cats.effect.{IO, Ref}
import fs2.Stream

final class TestNotificationListenerImpl(
    listener: KafkaListener[Notification],
    stateRef: Ref[IO, TestNotificationListenerImpl.NotificationListenerState],
) extends NotificationListener:
  def stream: Stream[IO, Unit] =
    listener.listen: notification =>
      stateRef.update: currentState =>
        currentState.copy(notification :: currentState.notifications)

object TestNotificationListenerImpl:
  final case class NotificationListenerState(notifications: List[Notification])

  object NotificationListenerState:
    val empty: NotificationListenerState = NotificationListenerState(List.empty)
