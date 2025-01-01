package es.eriktorr.pager
package api

import application.TelegramConfig
import commons.error.HandledError

import cats.effect.IO
import cats.implicits.toTraverseOps
import org.http4s.Uri
import org.http4s.client.Client

trait Broadcaster:
  def broadcast(notification: Notification): IO[Unit]

object Broadcaster:
  final class TelegramBot(httpClient: Client[IO], apiToken: TelegramConfig.ApiToken):
    def broadcast(notification: Notification): IO[Unit] = for
      uri <- IO.fromEither(Uri.fromString(s"https://api.telegram.org/bot$apiToken/sendMessage"))
      _ <- notification.addressees.traverse { addressee =>
        val target = uri
          .withQueryParam("chat_id", addressee.chatId.toLong)
          .withQueryParam(
            "text",
            s"Hi ${addressee.name}, the new version ${notification.version} of ${notification.projectName} is now available",
          )
        httpClient.expectOr[String](target)(response =>
          response
            .as[String]
            .flatMap(body =>
              IO.raiseError(TelegramBot.TelegramBotError(response.status.code, body)),
            ),
        )
      }
    yield ()

  object TelegramBot:
    final case class TelegramBotError(statusCode: Int, body: String)
        extends HandledError(s"Request failed with status $statusCode and body $body", None)
