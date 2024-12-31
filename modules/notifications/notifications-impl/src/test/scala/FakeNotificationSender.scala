package es.eriktorr.pager

import FakeNotificationSender.InMemory.NotificationSenderState

import cats.effect.{IO, Ref}

sealed abstract class FakeNotificationSender extends NotificationSender[String, String, String]

object FakeNotificationSender:
  object Pure extends FakeNotificationSender:
    override def send(precondition: String, notification: String): IO[String] =
      IO.pure(s"notified->{$precondition,$notification}")

  final class InMemory(stateRef: Ref[IO, NotificationSenderState]) extends FakeNotificationSender:
    override def send(precondition: String, notification: String): IO[String] =
      val newValue = s"notified->{$precondition,$notification}"
      stateRef.update: currentState =>
        currentState.copy(newValue :: currentState.notifications)
      *> IO.pure(newValue)

  object InMemory:
    final case class NotificationSenderState(notifications: List[String])

    object NotificationSenderState:
      val empty: NotificationSenderState = NotificationSenderState(List.empty)
