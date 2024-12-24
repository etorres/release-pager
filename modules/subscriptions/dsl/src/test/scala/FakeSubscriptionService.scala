package es.eriktorr.pager

import cats.effect.IO

final class FakeSubscriptionService(subscriptions: Map[Int, List[String]])
    extends SubscriptionService[String, Int, String, String]:
  override def subscribe(chatId: String, repository: Int): IO[String] =
    IO.raiseError(IllegalArgumentException("unsupported operation"))

  override def unsubscribe(subscriptionId: String): IO[String] =
    IO.raiseError(IllegalArgumentException("unsupported operation"))

  override def subscribersOf(repository: Int): IO[List[String]] =
    IO.pure(subscriptions.getOrElse(repository, List.empty))
