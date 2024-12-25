package es.eriktorr.pager

import cats.effect.IO

trait SubscriptionService[Repository, Subscriber, SubscriberId, Subscription]:
  def subscribe(repository: Repository, subscriberId: SubscriberId): IO[Subscription]

  def unsubscribe(repository: Repository, subscriberId: SubscriberId): IO[Subscription]

  def subscribersOf(repository: Repository): IO[List[Subscriber]]
