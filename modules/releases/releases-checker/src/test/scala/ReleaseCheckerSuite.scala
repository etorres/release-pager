package es.eriktorr.pager

import munit.CatsEffectSuite

final class ReleaseCheckerSuite extends CatsEffectSuite:
  test("should check for new versions released and notify subscribers"):
    val repositories = (1 to 4).toList
    val subscriptions = repositories
      .map(repository => repository -> (5 to 6).toList.map(chatId => s"chatId->$chatId"))
      .toMap
    val releaseChecker = TestReleaseChecker.Pure(repositories, subscriptions)
    val expected = Some(TestReleaseChecker.expectedFrom(subscriptions).fold("")(_ + _))
    releaseChecker.checkAndNotify.value.assertEquals(expected)
