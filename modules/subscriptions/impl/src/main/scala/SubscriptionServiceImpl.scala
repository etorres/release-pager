package es.eriktorr.pager

import cats.effect.IO
import cats.implicits.{catsSyntaxEither, catsSyntaxTuple3Parallel, toTraverseOps}
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import io.github.iltotore.iron.cats.*

object SubscriptionServiceImpl:
  final class Postgres(transactor: HikariTransactor[IO])
      extends SubscriptionService[
        Repository,
        Subscriber,
        Subscriber.Id,
        Subscription,
      ]:
    override def subscribe(repository: Repository, subscriberId: Subscriber.Id): IO[Subscription] =
      val (_repositoryId: Long, _subscriberId: Long) = (repository.id, subscriberId)
      sql"""INSERT INTO subscriptions
           |(repository_id, subscriber_id)
           |values ($_repositoryId, $_subscriberId)""".stripMargin.update.run
        .transact(transactor)
        .map(_ > 0)
        .ifM(
          ifTrue = IO.pure(Subscription(repository.id, subscriberId)),
          ifFalse = IO.raiseError(IllegalArgumentException("Subscription failed")),
        )

    override def unsubscribe(
        repository: Repository,
        subscriberId: Subscriber.Id,
    ): IO[Subscription] =
      IO.pure(Subscription(repository.id, subscriberId)) // TODO

    override def subscribersOf(repository: Repository): IO[List[Subscriber]] =
      val repositoryId: Long = repository.id
      for
        rows <- sql"""SELECT
                     |  id, chat_id, name
                     |FROM subscribers
                     |JOIN subscriptions ON subscriber_id = subscribers.id
                     |WHERE repository_id = $repositoryId""".stripMargin
          .query[(Long, Long, String)]
          .to[List]
          .transact(transactor)
        subscriptions <- IO.fromEither(rows.traverse { case (id, chatId, name) =>
          (
            Subscriber.Id.eitherNec(id),
            Subscriber.ChatId.eitherNec(chatId),
            Subscriber.Name.eitherNec(name),
          ).parMapN(Subscriber.apply)
            .leftMap(errors => IllegalArgumentException(errors.reduceLeft(_ + ", " + _)))
        })
      yield subscriptions
