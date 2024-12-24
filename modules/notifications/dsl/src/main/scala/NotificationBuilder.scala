package es.eriktorr.pager

import cats.effect.IO

trait NotificationBuilder[Subscription, Version, Notification]:
  def make(subscriptions: List[Subscription], version: Version): IO[Notification]
