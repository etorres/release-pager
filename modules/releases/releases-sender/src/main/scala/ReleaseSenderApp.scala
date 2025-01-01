package es.eriktorr.pager

import api.Broadcaster
import application.{ReleaseSenderConfig, ReleaseSenderParams}
import streams.KafkaStreams.KafkaListener

import cats.effect.{ExitCode, IO}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object ReleaseSenderApp extends CommandIOApp(name = "release-sender", header = "Release Sender"):
  override def main: Opts[IO[ExitCode]] =
    (ReleaseSenderConfig.opts, ReleaseSenderParams.opts).mapN { case (config, params) =>
      program(config, params)
    }

  private def program(config: ReleaseSenderConfig, params: ReleaseSenderParams) = for
    logger <- Slf4jLogger.create[IO]
    given SelfAwareStructuredLogger[IO] = logger
    _ <- (for
      httpClient <- HttpClient(verbose = params.verbose).resource
      kafkaListener <- KafkaListener.resource[Notification](config.kafkaConfig)
    yield (httpClient, kafkaListener)).use: (httpClient, kafkaListener) =>
      val broadcaster = Broadcaster.TelegramBot(httpClient, config.telegramConfig.apiToken)
      val releaseSender =
        ReleaseSender(NotificationListenerImpl.Kafka(kafkaListener, broadcaster.broadcast))
      releaseSender.stream.compile.drain
  yield ExitCode.Success
