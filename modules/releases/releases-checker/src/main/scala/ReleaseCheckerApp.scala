package es.eriktorr.pager

import RepositoryServiceImpl.RepositoryServiceConfig
import api.HttpClient
import db.JdbcTransactor

import cats.effect.{ExitCode, IO}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.given
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object ReleaseCheckerApp extends CommandIOApp(name = "release-checker", header = "Release Checker"):
  override def main: Opts[IO[ExitCode]] =
    val kk = program()
    assert(kk == kk)
    ???

  private def program() = for
    logger <- Slf4jLogger.create[IO]
    given SelfAwareStructuredLogger[IO] = logger
    releaseChecker <- (for
      httpClient <- HttpClient(???).resource
      redis <- Redis[IO].utf8("redis://localhost")
      transactor <- JdbcTransactor(???).resource
    yield (httpClient, transactor)).use: (httpClient, transactor) =>
      val releaseChecker = ReleaseChecker.impl(
        RepositoryServiceImpl.Postgres(transactor, RepositoryServiceConfig.default),
        ReleaseFinderImpl.MavenCentral(httpClient),
        SubscriptionServiceImpl.Postgres(transactor),
        NotificationBuilderImpl.Default(),
        NotificationSenderImpl.Redis(),
      )
      IO.pure(releaseChecker).flatMap(_ => IO.never)
  yield ExitCode.Success
