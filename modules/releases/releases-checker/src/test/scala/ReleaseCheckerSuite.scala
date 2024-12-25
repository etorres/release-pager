package es.eriktorr.pager

import munit.CatsEffectSuite

final class ReleaseCheckerSuite extends CatsEffectSuite:
  test("should check for new versions released and notify subscribers"):
    val isEven = (repository: Int) => repository % 2 == 0
    val repositories = (1 to 4).toList
    val subscriptions = repositories
      .map(repository => repository -> (5 to 6).toList.map(chatId => s"chatId->$chatId"))
      .toMap
    val expected =
      Some(
        subscriptions
          .filter { case (repository, _) => isEven(repository) }
          .toList
          .map { case (repository, subscriptions) =>
            val chatIds = subscriptions.sorted.mkString("[", ",", "]")
            s"""notified->{
               |  updated->{
               |    repository:$repository, version:released->{repository:$repository}
               |  },
               |  notification->{
               |    subscribers:$chatIds, repository:$repository, version:released->{repository:$repository}
               |  }
               |}""".stripMargin.replaceAll("\\s", "").nn.replaceAll("\\R", "").nn
          }
          .reduce(_ + _),
      )
    ReleaseChecker(
      FakeRepositoryService(repositories),
      FakeReleaseFinder(isEven),
      FakeSubscriptionService(subscriptions),
      FakeNotificationBuilder,
      FakeNotificationSender,
    ).checkAndNotify.value.assertEquals(expected)
