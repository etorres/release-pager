package es.eriktorr.pager
package api

import cats.effect.IO
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.fs2.Scheduler

import scala.concurrent.duration.FiniteDuration

final class FakeReleaseCheckerScheduler(
    calendarEvent: CalEvent,
    releaseChecker: TestReleaseChecker.InMemory,
    checkFrequency: FiniteDuration,
)(using scheduler: Scheduler[IO])
    extends ReleaseCheckerScheduler[Int, String, String, String, String, String](
      calendarEvent,
      releaseChecker,
      checkFrequency,
    )
