package es.eriktorr.pager

import FakeNotificationSender.InMemory.NotificationSenderState

import cats.effect.{IO, Ref}

final class FakeReleaseChecker(
    repositories: List[Int],
    filter: Int => Boolean,
    subscriptions: Map[Int, List[String]],
    maybeStateRef: Option[Ref[IO, NotificationSenderState]] = None,
) extends ReleaseChecker(
      FakeRepositoryService(repositories),
      FakeReleaseFinder(filter),
      FakeSubscriptionService(subscriptions),
      FakeNotificationBuilder,
      maybeStateRef match
        case Some(stateRef) => FakeNotificationSender.InMemory(stateRef)
        case None => FakeNotificationSender.Pure,
    )
