package es.eriktorr.pager

import cats.effect.IO

final class FakeSubscriptionService(subscriptions: Map[Int, List[String]])
    extends SubscriptionService[Int, String, String, String]:
  override def subscribe(repository: Int, subscriberId: String): IO[String] =
    IO.raiseError(IllegalArgumentException("unsupported operation"))

  override def unsubscribe(repository: Int, subscriberId: String): IO[String] =
    IO.raiseError(IllegalArgumentException("unsupported operation"))

  override def subscribersOf(repository: Int): IO[List[String]] =
    IO.pure(subscriptions.getOrElse(repository, List.empty))
