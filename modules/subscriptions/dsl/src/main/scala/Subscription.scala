package es.eriktorr.pager

import commons.std.TSIDGen

import cats.effect.IO
import cats.implicits.catsSyntaxEither
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.any.Pure
import io.github.iltotore.iron.constraint.numeric.Positive0
import io.hypersistence.tsid.TSID

final case class Subscription(
    id: Subscription.Id,
    chatId: Subscription.ChatId,
    repositoryId: Repository.Id,
)

object Subscription:
  opaque type Id <: Long :| Positive0 = Long :| Positive0

  object Id extends RefinedTypeOps[Long, Positive0, Id]:
    def io(tsid: TSID): IO[Id] =
      IO.fromEither(Id.either(tsid.toLong.nn).leftMap(IllegalArgumentException(_)))

  opaque type ChatId <: Long :| Pure = Long :| Pure

  object ChatId extends RefinedTypeOps[Long, Pure, ChatId]

  def from(chatId: Subscription.ChatId, repositoryId: Repository.Id)(using
      tsidGen: TSIDGen[IO],
  ): IO[Subscription] = for
    tsid <- tsidGen.randomTSID
    id <- Subscription.Id.io(tsid)
  yield Subscription(id, chatId, repositoryId)
