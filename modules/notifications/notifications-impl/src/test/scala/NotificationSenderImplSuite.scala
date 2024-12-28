package es.eriktorr.pager

import NotificationGenerators.notificationGen
import spec.KafkaSuite
import streams.TestNotificationListenerImpl
import streams.TestNotificationListenerImpl.NotificationListenerState

import cats.effect.{IO, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import org.scalacheck.effect.PropF.forAllF

final class NotificationSenderImplSuite extends KafkaSuite:
  test("should send a notification"):
    forAllF(notificationGen()): notification =>
      (testListener.resource[Notification], testSender.resource[Notification]).tupled.use:
        case (listener, sender) =>
          (for
            stateRef <- Ref.of[IO, NotificationListenerState](NotificationListenerState.empty)
            notificationSender = NotificationSenderImpl.Kafka(sender)
            testNotificationListener = TestNotificationListenerImpl(listener, stateRef)
            _ <- notificationSender.send(notification)
            _ <- testNotificationListener.stream.take(1L).compile.drain
            obtained <- stateRef.get
          yield obtained.notifications).assertEquals(List(notification))
