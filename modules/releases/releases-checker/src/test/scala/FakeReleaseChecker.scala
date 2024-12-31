package es.eriktorr.pager

import FakeNotificationSender.InMemory.NotificationSenderState

import cats.effect.{IO, Ref}

sealed abstract class FakeReleaseChecker(
    repositories: List[Int],
    filter: Int => Boolean,
    subscriptions: Map[Int, List[String]],
    maybeStateRef: Option[Ref[IO, NotificationSenderState]],
) extends ReleaseChecker(
      FakeRepositoryService(repositories),
      FakeReleaseFinder(filter),
      FakeSubscriptionService(subscriptions),
      FakeNotificationBuilder,
      maybeStateRef match
        case Some(stateRef) => FakeNotificationSender.InMemory(stateRef)
        case None => FakeNotificationSender.Pure,
    )

object FakeReleaseChecker:
  final class Pure(repositories: List[Int], subscriptions: Map[Int, List[String]])
      extends FakeReleaseChecker(repositories, isEven, subscriptions, None)

  final class InMemory(
      repositories: List[Int],
      subscriptions: Map[Int, List[String]],
      stateRef: Ref[IO, NotificationSenderState],
  ) extends FakeReleaseChecker(repositories, isEven, subscriptions, Some(stateRef))

  private lazy val isEven = (repository: Int) => repository % 2 == 0

  def expectedFrom(subscriptions: Map[Int, List[String]]): List[String] =
    import scala.language.unsafeNulls
    subscriptions
      .filter { case (repository, _) => isEven(repository) }
      .toList
      .map:
        case (repository, subscriptions) =>
          val chatIds = subscriptions.sorted.mkString("[", ",", "]")
          s"""notified->{
             |  updated->{
             |    repository:$repository, version:released->{repository:$repository}
             |  },
             |  notification->{
             |    subscribers:$chatIds, repository:$repository, version:released->{repository:$repository}
             |  }
             |}""".stripMargin.replaceAll("\\s", "").replaceAll("\\R", "")
