package es.eriktorr.pager

import FakeNotificationListener.NotificationListenerState
import NotificationGenerators.notificationGen
import ReleaseSenderSuite.{testCaseGen, TestCase}
import commons.spec.CollectionGenerators.nDistinct

import cats.effect.{IO, Ref}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class ReleaseSenderSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  test("should go over all notifications"):
    forAllF(testCaseGen):
      case TestCase(notifications) =>
        (for
          stateRef <- Ref.of[IO, NotificationListenerState](NotificationListenerState.empty)
          releaseSender = ReleaseSender(FakeNotificationListener(stateRef))
          _ <- releaseSender.stream.take(1L).compile.drain
          obtained <- stateRef.get
        yield obtained.notifications).assertEquals(List.empty[Notification])

object ReleaseSenderSuite:
  final private case class TestCase(notifications: List[Notification])

  private val testCaseGen = for
    size <- Gen.choose(1, 3)
    notifications <- nDistinct(size, notificationGen())
  yield TestCase(notifications)
