package es.eriktorr.pager

import FakeNotificationListener.NotificationListenerState

import cats.effect.{IO, Ref}
import fs2.Stream

final class FakeNotificationListener(stateRef: Ref[IO, NotificationListenerState])
    extends NotificationListener:
  override def stream: Stream[IO, Unit] =
    Stream.eval(stateRef.update(_ => NotificationListenerState.empty))

object FakeNotificationListener:
  final case class NotificationListenerState(notifications: List[Notification])

  object NotificationListenerState:
    val empty: NotificationListenerState = NotificationListenerState(List.empty)
