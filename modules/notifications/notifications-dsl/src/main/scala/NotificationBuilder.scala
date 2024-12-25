package es.eriktorr.pager

import cats.effect.IO

trait NotificationBuilder[Subscriber, Repository, Version, Notification]:
  def make(
      subscribers: List[Subscriber],
      repository: Repository,
      version: Version,
  ): IO[Notification]
