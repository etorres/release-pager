package es.eriktorr.pager

import cats.Semigroup
import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import cats.implicits.catsSyntaxParallelFlatTraverse1

abstract class ReleaseChecker[
    Repository,
    Version,
    Updated,
    Subscriber,
    Notification,
    Notified: Semigroup,
](
    repositoryService: RepositoryService[Repository, Version, Updated],
    releaseFinder: ReleaseFinder[Repository, Version],
    subscriptionService: SubscriptionService[Repository, ?, ?, Subscriber],
    notificationBuilder: NotificationBuilder[Subscriber, Repository, Version, Notification],
    notificationSender: NotificationSender[Updated, Notification, Notified],
):
  def checkAndNotify: OptionT[IO, Notified] = OptionT:
    for
      repositories <- repositoryService.findEarliestUpdates()
      notifiedList <- repositories.parFlatTraverse: repository =>
        (for
          version <- releaseFinder.findNewVersionOf(repository)
          notified <- OptionT.liftF:
            for
              subscribers <- subscriptionService.subscribersOf(repository)
              notification <- notificationBuilder.make(subscribers, repository, version)
              updated <- repositoryService.update(repository, version)
              notified <- notificationSender.send(updated, notification)
            yield notified
        yield notified).value.map(_.toList)
      maybeNotified = NonEmptyList.fromList(notifiedList).map(_.reduce)
    yield maybeNotified

object ReleaseChecker:
  final class Default(
      repositoryService: RepositoryService[Repository, Repository.Version, Unit],
      releaseFinder: ReleaseFinder[Repository, Repository.Version],
      subscriptionService: SubscriptionService[Repository, Subscriber.Id, Subscription, Subscriber],
      notificationBuilder: NotificationBuilder[
        Subscriber,
        Repository,
        Repository.Version,
        Notification,
      ],
      notificationSender: NotificationSender[Unit, Notification, Unit],
  ) extends ReleaseChecker[Repository, Repository.Version, Unit, Subscriber, Notification, Unit](
        repositoryService,
        releaseFinder,
        subscriptionService,
        notificationBuilder,
        notificationSender,
      )
