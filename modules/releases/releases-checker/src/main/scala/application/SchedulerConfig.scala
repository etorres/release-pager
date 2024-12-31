package es.eriktorr.pager
package application

import cats.Show
import com.github.eikek.calev.CalEvent

import scala.concurrent.duration.FiniteDuration

final case class SchedulerConfig(calendarEvent: CalEvent, checkFrequency: FiniteDuration)

object SchedulerConfig:
  given Show[SchedulerConfig] =
    import scala.language.unsafeNulls
    Show.show(config =>
      s"""scheduler-calendar-event: ${config.calendarEvent.asString},
         | scheduler-check-frequency: ${config.checkFrequency}""".stripMargin.replaceAll("\\R", ""),
    )
