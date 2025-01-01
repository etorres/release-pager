package es.eriktorr.pager
package api

import application.TelegramConfig
import commons.spec.TestFilters.online

import cats.effect.std.Env
import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class BroadcasterSuite extends CatsEffectSuite:
  test("should send a message using a Telegram bot".tag(online)):
    (for
      maybeApiToken <- Env[IO].get("SBX_TELEGRAM_API_TOKEN")
      apiToken <- IO.fromOption(maybeApiToken.flatMap(TelegramConfig.ApiToken.option))(
        IllegalArgumentException("Invalid API Token"),
      )
      _ <- (for
        logger <- Resource.eval(Slf4jLogger.fromName[IO]("debug-logger"))
        given SelfAwareStructuredLogger[IO] = logger
        httpClient <- HttpClient(verbose = true).resource
      yield httpClient).use: httpClient =>
        val telegramBot = Broadcaster.TelegramBot(httpClient, apiToken)
        telegramBot.broadcast(
          Notification(
            List(
              Notification.Addressee(
                Notification.ChatId.applyUnsafe(123456L),
                Notification.Name.applyUnsafe("sbx"),
              ),
            ),
            Notification.ProjectName.applyUnsafe("es.eriktorr.pager:release-sender"),
            Notification.Version.applyUnsafe("1.0.0"),
          ),
        )
    yield ()).assertEquals((), "Server response is OK")
