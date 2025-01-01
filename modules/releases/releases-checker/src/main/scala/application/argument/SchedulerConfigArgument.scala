package es.eriktorr.pager
package application.argument

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import com.github.eikek.calev.CalEvent
import com.monovore.decline.Argument

trait SchedulerConfigArgument:
  given calEventArgument: Argument[CalEvent] = new Argument[CalEvent]:
    override def read(string: String): ValidatedNel[String, CalEvent] = CalEvent.parse(string) match
      case Left(error) => error.invalidNel
      case Right(value) => value.validNel

    override def defaultMetavar: String = "event"

object SchedulerConfigArgument extends SchedulerConfigArgument
