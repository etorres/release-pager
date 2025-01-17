package es.eriktorr.pager

import commons.refined.Constraints.NonEmptyString

import cats.Order
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.any.Pure

final case class Notification(
    addressees: List[Notification.Addressee],
    projectName: Notification.ProjectName,
    version: Notification.Version,
)

object Notification:
  opaque type ChatId <: Long :| Pure = Long :| Pure

  object ChatId extends RefinedTypeOps[Long, Pure, ChatId]

  opaque type Name <: String :| NonEmptyString = String :| NonEmptyString

  object Name extends RefinedTypeOps[String, NonEmptyString, Name]

  opaque type ProjectName <: String :| NonEmptyString = String :| NonEmptyString

  object ProjectName extends RefinedTypeOps[String, NonEmptyString, ProjectName]

  opaque type Version <: String :| NonEmptyString = String :| NonEmptyString

  object Version extends RefinedTypeOps[String, NonEmptyString, Version]

  final case class Addressee(chatId: ChatId, name: Name)

  object Addressee:
    given Decoder[Addressee] = deriveDecoder

    given Encoder[Addressee] = deriveEncoder

  given Decoder[Notification] = deriveDecoder

  given Encoder[Notification] = deriveEncoder

  given Order[Notification] =
    Order.by[Notification, (String, String)](x => (x.projectName, x.version))
