package es.eriktorr.pager

import ReleaseFinderImpl.MavenCentralError
import ReleaseFinderImpl.MavenCentralError.RepositoryNotFound
import Repository.*
import api.HttpClient
import commons.spec.TestFilters.online
import commons.std.TSIDGen

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt

final class ReleaseFinderImplSuite extends CatsEffectSuite:
  test("should get the most recent version released of a repository".tag(online)):
    testWith("com.google.inject", "guice", "6.0.0").assertEquals(
      Some(Version.applyUnsafe("7.0.0")),
    )

  test("returns none when versions are the same".tag(online)):
    testWith("com.google.inject", "guice", "7.0.0").assertEquals(None)

  test("should fail with an error when the repository name format is not supported"):
    val (groupId, artifactId) = ("com.google.inject", "no-juice")
    interceptMessageIO[RepositoryNotFound](s"Repository not found: $groupId:$artifactId")(
      testWith(groupId, artifactId, "1.0.0"),
    )

  private def testWith(groupId: String, artifactId: String, version: String) = (for
    logger <- Resource.eval(Slf4jLogger.fromName[IO]("debug-logger"))
    httpClient <- HttpClient(30.seconds)(using logger).resource
    releaseFinder = ReleaseFinderImpl.MavenCentral(httpClient)
  yield releaseFinder)
    .use: releaseFinder =>
      for
        tsid <- TSIDGen[IO].randomTSID
        repository = Repository(
          Id.applyUnsafe(tsid.toLong),
          GroupId.applyUnsafe(groupId),
          ArtifactId.applyUnsafe(artifactId),
          Version.applyUnsafe(version),
        )
        newVersion <- releaseFinder.findNewVersionOf(repository).value
      yield newVersion
