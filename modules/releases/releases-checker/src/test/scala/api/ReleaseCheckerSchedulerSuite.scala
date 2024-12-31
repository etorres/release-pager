package es.eriktorr.pager
package api

import FakeNotificationSender.InMemory.NotificationSenderState
import commons.spec.CollectionGenerators.nDistinct
import commons.spec.StringGenerators.alphaNumericStringBetween

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.fs2.Scheduler
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.forAllF
import org.scalacheck.{Gen, Test}

import scala.concurrent.duration.DurationInt

final class ReleaseCheckerSchedulerSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  test("should schedule a release check"):
    forAllF(ReleaseCheckerSchedulerSuite.testCaseGen):
      case ReleaseCheckerSchedulerSuite.TestCase(repositories, filter, subscriptions) =>
        given Scheduler[IO] = Scheduler.systemDefault[IO]
        (for
          stateRef <- Ref.of[IO, NotificationSenderState](NotificationSenderState.empty)
          releaseChecker = FakeReleaseChecker.InMemory(repositories, subscriptions, stateRef)
          releaseCheckerScheduler =
            ReleaseCheckerScheduler(
              CalEvent.unsafe("*-*-* *:*:*"),
              releaseChecker,
              1.minutes,
            )
          _ <- releaseCheckerScheduler.stream.take(1L).compile.drain
          obtained <- stateRef.get
        yield obtained.notifications.sorted)
          .assertEquals(FakeReleaseChecker.expectedFrom(subscriptions).sorted)

  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(3)

object ReleaseCheckerSchedulerSuite:
  final private case class TestCase(
      repositories: List[Int],
      filter: Int => Boolean,
      subscriptions: Map[Int, List[String]],
  )

  private val testCaseGen = for
    size <- Gen.choose(1, 3)
    repositories <- nDistinct(size, Gen.choose(0, Int.MaxValue))
    subscriptions <- repositories
      .traverse(repository =>
        for
          subscribers <- Gen.choose(1, 3)
          subscriptions <- nDistinct(subscribers, alphaNumericStringBetween(3, 5))
        yield repository -> subscriptions,
      )
      .map(_.toMap)
    isEven = (repository: Int) => repository % 2 == 0
  yield TestCase(repositories, isEven, subscriptions)
