package es.eriktorr.pager

import NotificationGenerators.{notificationGen, projectNameGen, versionGen}
import NotificationSenderImplSuite.{testCaseGen, TestCase}
import commons.spec.CollectionGenerators.nDistinct
import spec.KafkaSuite
import streams.TestNotificationConsumer
import streams.TestNotificationConsumer.NotificationConsumerState

import cats.data.NonEmptyList
import cats.effect.{IO, Ref}
import cats.implicits.{catsKernelOrderingForOrder, toFoldableOps, toTraverseOps}
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.effect.PropF.forAllF

final class NotificationSenderImplSuite extends KafkaSuite:
  test("should send notifications"):
    forAllF(testCaseGen):
      case TestCase(notifications, expected) =>
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
              yield obtained.notifications.sorted).assertEquals(expected.sorted)

object NotificationSenderImplSuite:
  final private case class TestCase(
      notifications: NonEmptyList[Notification],
      expected: List[Notification],
  )

  private val testCaseGen = for
    projectNames <- nDistinct(2, projectNameGen)
    versions <- Gen.listOfN(3, versionGen)
    notifications <- versions.traverse(version =>
      notificationGen(projectNameGen = Gen.oneOf(projectNames), versionGen = version),
    )
  yield TestCase(NonEmptyList.fromListUnsafe(notifications), notifications)
