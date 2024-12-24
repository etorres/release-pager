package es.eriktorr.pager

import commons.error.HandledError

import cats.data.OptionT
import cats.effect.IO
import cats.implicits.{catsSyntaxTuple3Semigroupal, catsSyntaxTuple8Semigroupal}
import io.circe.{Decoder, HCursor}
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.client.Client
import org.http4s.{Method, Request, Uri}

object ReleaseFinderImpl:
  /** Search for new released versions in Maven Central Repository.
    * @see
    *   [[https://central.sonatype.org/search/rest-api-guide/ REST API]]
    * @param httpClient
    *   HTTP client
    */
  final class MavenCentral(httpClient: Client[IO])
      extends ReleaseFinder[Repository, Repository.Version]:
    override def findNewVersionOf(repository: Repository): OptionT[IO, Repository.Version] =
      val (groupId, artifactId) = (repository.groupId, repository.artifactId)
      val request = Request[IO](
        Method.GET,
        Uri.unsafeFromString(
          s"https://search.maven.org/solrsearch/select?q=g:$groupId+AND+a:$artifactId&rows=10&wt=json",
        ),
      )
      OptionT(for
        searchResponse <- httpClient.expect[MavenCentral.SearchResponse](request)
        maybeVersion = searchResponse.response.docs.headOption.flatMap(doc =>
          Repository.Version.option(doc.latestVersion),
        )
        version <- maybeVersion match
          case Some(version) => IO.pure(version)
          case None => IO.raiseError(MavenCentralError.RepositoryNotFound(repository))
        newVersion = if version.value != repository.version.value then Some(version) else None
      yield newVersion)

  object MavenCentral:
    final case class Doc(
        id: String,
        g: String,
        a: String,
        latestVersion: String,
        repositoryId: String,
        p: String,
        timestamp: Long,
        versionCount: Int,
    )

    object Doc:
      given Decoder[Doc] = (cursor: HCursor) =>
        (
          cursor.downField("id").as[String],
          cursor.downField("g").as[String],
          cursor.downField("a").as[String],
          cursor.downField("latestVersion").as[String],
          cursor.downField("repositoryId").as[String],
          cursor.downField("p").as[String],
          cursor.downField("timestamp").as[Long],
          cursor.downField("versionCount").as[Int],
        ).mapN(Doc.apply)

    final case class Response(numFound: Int, start: Int, docs: List[Doc])

    object Response:
      given Decoder[Response] = (cursor: HCursor) =>
        (
          cursor.downField("numFound").as[Int],
          cursor.downField("start").as[Int],
          cursor.downField("docs").as[List[Doc]],
        ).mapN(Response.apply)

    final case class SearchResponse(response: Response)

    object SearchResponse:
      given Decoder[SearchResponse] = (cursor: HCursor) =>
        cursor.downField("response").as[Response].map(SearchResponse.apply)

  sealed abstract class MavenCentralError(message: String) extends HandledError(message)

  object MavenCentralError:
    final case class RepositoryNotFound(repository: Repository)
        extends MavenCentralError(s"Repository not found: ${repository.projectName}")
