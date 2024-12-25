package es.eriktorr.pager

import cats.effect.IO
import cats.implicits.{catsSyntaxEither, catsSyntaxTuple2Parallel, toTraverseOps}
import io.github.iltotore.iron.cats.*

object NotificationBuilderImpl:
  final class Default
      extends NotificationBuilder[Subscriber, Repository, Repository.Version, Notification]:
    override def make(
        subscribers: List[Subscriber],
        repository: Repository,
        version: Repository.Version,
    ): IO[Notification] = for
      addressees <- IO.fromEither(
        subscribers.traverse(subscriber =>
          (
            Notification.ChatId.eitherNec(subscriber.chatId),
            Notification.Name.eitherNec(subscriber.name),
          ).parMapN(Notification.Addressee.apply)
            .leftMap(errors => IllegalArgumentException(errors.reduceLeft(_ + ", " + _))),
        ),
      )
      projectName = Notification.ProjectName.applyUnsafe(repository.projectName)
      newVersion = Notification.Version.applyUnsafe(version)
    yield Notification(addressees, projectName, newVersion)
