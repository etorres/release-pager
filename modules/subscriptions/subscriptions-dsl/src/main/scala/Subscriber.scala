package es.eriktorr.pager

import commons.refined.Constraints.NonEmptyString
import commons.std.TSIDGen

import cats.Order
import cats.effect.IO
import cats.implicits.catsSyntaxEither
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.any.Pure
import io.github.iltotore.iron.constraint.numeric.Positive0
import io.hypersistence.tsid.TSID

final case class Subscriber(
    id: Subscriber.Id,
    chatId: Subscriber.ChatId,
    name: Subscriber.Name,
)

object Subscriber:
  opaque type Id <: Long :| Positive0 = Long :| Positive0

  object Id extends RefinedTypeOps[Long, Positive0, Id]:
    def io(tsid: TSID): IO[Id] =
      IO.fromEither(Id.either(tsid.toLong.nn).leftMap(IllegalArgumentException(_)))

  opaque type ChatId <: Long :| Pure = Long :| Pure

  object ChatId extends RefinedTypeOps[Long, Pure, ChatId]

  opaque type Name <: String :| NonEmptyString = String :| NonEmptyString

  object Name extends RefinedTypeOps[String, NonEmptyString, Name]

  given Order[Subscriber] = Order.by[Subscriber, Long](x => x.id)

  def from(chatId: Subscriber.ChatId, name: Subscriber.Name)(using
      tsidGen: TSIDGen[IO],
  ): IO[Subscriber] = for
    tsid <- tsidGen.randomTSID
    id <- Subscriber.Id.io(tsid)
  yield Subscriber(id, chatId, name)
