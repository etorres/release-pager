package es.eriktorr.pager

import cats.Semigroup
import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import cats.implicits.catsSyntaxParallelFlatTraverse1

abstract class ReleaseChecker[
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
    notificationBuilder: NotificationBuilder[Subscriber, Repository, Version, Notification],
    notificationSender: NotificationSender[Notification, Notified, Updated],
):
  def checkAndNotify: OptionT[IO, Notified] = OptionT(for
    repositories <- repositoryService.findEarliestUpdates()
    notifiedList <- repositories
      .parFlatTraverse: repository =>
        (for
          version <- releaseFinder.findNewVersionOf(repository)
          notified <- OptionT.liftF(for
            updated <- repositoryService.update(repository, version)
            subscribers <- subscriptionService.subscribersOf(repository)
            notification <- notificationBuilder.make(subscribers, repository, version)
            notified <- notificationSender.send(updated, notification)
          yield notified)
        yield notified).value.map(_.toList)
    maybeNotified = NonEmptyList.fromList(notifiedList).map(_.reduce)
  yield maybeNotified)

object ReleaseChecker:
  final class Default(
      repositoryService: RepositoryService[Repository, Unit, Repository.Version],
      releaseFinder: ReleaseFinder[Repository, Repository.Version],
      subscriptionService: SubscriptionService[Repository, Subscriber, Subscriber.Id, Subscription],
      notificationBuilder: NotificationBuilder[
        Subscriber,
        Repository,
        Repository.Version,
        Notification,
      ],
      notificationSender: NotificationSender[Notification, Unit, Unit],
  ) extends ReleaseChecker[Notification, Unit, Repository, Subscriber, Unit, Repository.Version](
        repositoryService,
        releaseFinder,
        subscriptionService,
        notificationBuilder,
        notificationSender,
      )
