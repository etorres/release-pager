package es.eriktorr.pager

import RepositoryServiceImpl.RepositoryServiceConfig
import api.HttpClient
import application.{ReleaseCheckerConfig, ReleaseCheckerParams}
import commons.std.TSIDGen
import db.JdbcTransactor
import streams.KafkaStreams.KafkaSender

import cats.effect.{ExitCode, IO}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object ReleaseCheckerApp extends CommandIOApp(name = "release-checker", header = "Release Checker"):
  override def main: Opts[IO[ExitCode]] =
    (ReleaseCheckerConfig.opts, ReleaseCheckerParams.opts).mapN { case (config, params) =>
      program(config, params)
    }

  private def program(config: ReleaseCheckerConfig, params: ReleaseCheckerParams) = for
    logger <- Slf4jLogger.create[IO]
    given SelfAwareStructuredLogger[IO] = logger
    given TSIDGen[IO] = TSIDGen[IO]
    releaseChecker <- (for
      httpClient <- HttpClient(verbose = params.verbose).resource
      kafkaSender <- KafkaSender.resource[Notification](config.kafkaConfig)
      transactor <- JdbcTransactor(config.jdbcConfig).resource
    yield (httpClient, kafkaSender, transactor)).use: (httpClient, kafkaSender, transactor) =>
      val releaseChecker = ReleaseChecker.impl(
        RepositoryServiceImpl.Postgres(transactor, RepositoryServiceConfig.default),
        ReleaseFinderImpl.MavenCentral(httpClient),
        SubscriptionServiceImpl.Postgres(transactor),
        NotificationBuilderImpl.Default(),
        NotificationSenderImpl.Kafka(kafkaSender),
      )
      IO.pure(releaseChecker).flatMap(_ => IO.never)
  yield ExitCode.Success
