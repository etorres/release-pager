package es.eriktorr.pager
package application

import commons.refined.Constraints.NonEmptyString

import io.github.iltotore.iron.*

final case class TelegramConfig(apiToken: TelegramConfig.ApiToken)

object TelegramConfig:
  opaque type ApiToken <: String :| NonEmptyString = String :| NonEmptyString

  object ApiToken extends RefinedTypeOps[String, NonEmptyString, ApiToken]
