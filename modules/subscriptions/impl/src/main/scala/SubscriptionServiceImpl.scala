package es.eriktorr.pager

import cats.effect.IO

object SubscriptionServiceImpl:
  final class Postgres
      extends SubscriptionService[
        Subscription.ChatId,
        Repository,
        Subscription.Id,
        Subscription,
      ]:
    override def subscribe(
        chatId: Subscription.ChatId,
        repository: Repository,
    ): IO[Subscription] = IO.pure(
      Subscription(
        Subscription.Id.applyUnsafe(659134184538493369L),
        chatId,
        repository.id,
      ),
    ) // TODO

    override def unsubscribe(subscriptionId: Subscription.Id): IO[Subscription] = IO.pure(
      Subscription(
        Subscription.Id.applyUnsafe(659134184538493369L),
        Subscription.ChatId.applyUnsafe(1L),
        Repository.Id.applyUnsafe(659134189395497634L),
      ),
    ) // TODO

    override def subscribersOf(repository: Repository): IO[List[Subscription]] =
      IO.pure(List.empty) // TODO
