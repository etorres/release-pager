package es.eriktorr.pager

import NotificationGenerators.notificationGen
import commons.spec.CollectionGenerators.nDistinct
import spec.KafkaSuite
import streams.TestNotificationConsumer
import streams.TestNotificationConsumer.NotificationConsumerState

import cats.effect.{IO, Ref}
import cats.implicits.{catsKernelOrderingForOrder, toFoldableOps}
import org.scalacheck.effect.PropF.forAllF

final class NotificationSenderImplSuite extends KafkaSuite:
  test("should send notifications"):
    forAllF(nDistinct(3, notificationGen())): notifications =>
      testStreams
        .resource[Notification]
        .use:
          case (sender, listener) =>
            (for
              stateRef <- Ref.of[IO, NotificationConsumerState](NotificationConsumerState.empty)
              notificationSender = NotificationSenderImpl.Kafka(sender)
              notificationListener = NotificationListenerImpl.Kafka(
                listener,
                TestNotificationConsumer(stateRef).consume,
              )
              _ <- notifications.traverse_(notificationSender.send)
              _ <- notificationListener.stream.take(1L).compile.drain
              obtained <- stateRef.get
            yield obtained.notifications.sorted).assertEquals(notifications.sorted)
