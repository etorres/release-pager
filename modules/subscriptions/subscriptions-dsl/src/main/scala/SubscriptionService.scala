package es.eriktorr.pager

import cats.effect.IO

trait SubscriptionService[Repository, SubscriberId, Subscription, Subscriber]:
  def subscribe(repository: Repository, subscriberId: SubscriberId): IO[Subscription]

  def unsubscribe(repository: Repository, subscriberId: SubscriberId): IO[Subscription]

  def subscribersOf(repository: Repository): IO[List[Subscriber]]
