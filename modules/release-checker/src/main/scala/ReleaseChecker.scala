package es.eriktorr.pager

import cats.Semigroup
import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import cats.implicits.{catsSyntaxParallelTraverse1, toTraverseOps}

final class ReleaseChecker[
    Notification,
    Notified: Semigroup,
    Repository,
    Subscriber,
    Updated,
    Version,
](
    repositoryService: RepositoryService[Repository, Updated, Version],
    releaseFinder: ReleaseFinder[Repository, Version],
    subscriptionService: SubscriptionService[Repository, Subscriber, ?, ?],
    notificationBuilder: NotificationBuilder[Subscriber, Version, Notification],
    notificationSender: NotificationSender[Notification, Notified, Updated],
):
  def checkAndNotify: OptionT[IO, Notified] = OptionT(for
    repositories <- repositoryService.findEarliestUpdates()
    maybeNotifiedList <- repositories
      .parTraverse: repository =>
        for
          maybeVersion <- releaseFinder.findNewVersionOf(repository).value
          maybeNotified <- maybeVersion.traverse(version =>
            for
              updated <- repositoryService.update(repository, version)
              subscribers <- subscriptionService.subscribersOf(repository)
              notification <- notificationBuilder.make(subscribers, version)
              notified <- notificationSender.send(updated, notification)
            yield notified,
          )
        yield maybeNotified
    maybeNotified = NonEmptyList
      .fromList(maybeNotifiedList.collect { case Some(value) => value })
      .map(_.reduce)
  yield maybeNotified)
