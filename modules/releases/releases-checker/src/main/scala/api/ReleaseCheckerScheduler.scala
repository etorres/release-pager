package es.eriktorr.pager
package api

import Repository.Version

import cats.Semigroup
import cats.effect.IO
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.fs2.Scheduler
import fs2.Stream

import scala.concurrent.duration.FiniteDuration

abstract class ReleaseCheckerScheduler[
    Repository,
    Version,
    Updated,
    Subscriber,
    Notification,
    Notified: Semigroup,
](
    calendarEvent: CalEvent,
    releaseChecker: ReleaseChecker[
      Repository,
      Version,
      Updated,
      Subscriber,
      Notification,
      Notified,
    ],
    checkFrequency: FiniteDuration,
)(using
    scheduler: Scheduler[IO],
):
  def stream: Stream[IO, Unit] = for
    trigger <- Stream.eval(IO.pure(calendarEvent))
    _ <- scheduler.awakeEvery(trigger) >> runCheck()
  yield ()

  private def runCheck() =
    Stream.eval(releaseChecker.checkAndNotify.value.void).timeout(checkFrequency)

object ReleaseCheckerScheduler:
  final class Default(
      calendarEvent: CalEvent,
      releaseChecker: ReleaseChecker.Default,
      checkFrequency: FiniteDuration,
  )(using
      scheduler: Scheduler[IO],
  ) extends ReleaseCheckerScheduler[
        Repository,
        Repository.Version,
        Unit,
        Subscriber,
        Notification,
        Unit,
      ](calendarEvent, releaseChecker, checkFrequency)
