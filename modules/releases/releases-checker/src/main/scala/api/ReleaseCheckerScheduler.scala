package es.eriktorr.pager
package api

import cats.Semigroup
import cats.effect.IO
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.fs2.Scheduler
import fs2.Stream

import scala.concurrent.duration.FiniteDuration

final class ReleaseCheckerScheduler[
    Notification,
    Notified: Semigroup,
    Repository,
    Subscriber,
    Updated,
    Version,
](
    calendarEvent: CalEvent,
    releaseChecker: ReleaseChecker[
      Notification,
      Notified,
      Repository,
      Subscriber,
      Updated,
      Version,
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