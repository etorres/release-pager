package es.eriktorr.pager

import cats.effect.IO

trait SubscriptionService[ChatId, Repository, SubscriptionId, Subscription]:
  def subscribe(chatId: ChatId, repository: Repository): IO[Subscription]

  def unsubscribe(subscriptionId: SubscriptionId): IO[Subscription]

  def subscribersOf(repository: Repository): IO[List[Subscription]]
