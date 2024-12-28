package es.eriktorr.pager

import NotificationGenerators.notificationGen
import commons.spec.CollectionGenerators.nDistinct
import spec.KafkaSuite
import streams.TestNotificationListenerImpl
import streams.TestNotificationListenerImpl.NotificationListenerState

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import org.scalacheck.effect.PropF.forAllF

final class NotificationSenderImplSuite extends KafkaSuite:
  test("should send a notification"):
    forAllF(nDistinct(3, notificationGen())): notifications =>
      testStreams
        .resource[Notification]
        .use:
          case (sender, listener) =>
            (for
              stateRef <- Ref.of[IO, NotificationListenerState](NotificationListenerState.empty)
              notificationSender = NotificationSenderImpl.Kafka(sender)
              testNotificationListener = TestNotificationListenerImpl(listener, stateRef)
              _ <- notifications.traverse(notificationSender.send)
              _ <- testNotificationListener.stream.take(2L).compile.drain
              obtained <- stateRef.get
            yield obtained.notifications).assertEquals(List(notifications.drop(1)))
