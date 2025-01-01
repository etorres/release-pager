package es.eriktorr.pager

import Repository.{ArtifactId, GroupId, Id, Version}
import RepositoryGenerators.{projectGen, versionGen}
import RepositoryServiceImplSuite.{findTestCaseGen, updateTestCaseGen, TestCase}
import SubscriptionGenerator.idGen
import commons.spec.CollectionGenerators.{nDistinct, nDistinctExcluding}
import commons.spec.TemporalGenerators.{localDateTimeAfter, localDateTimeBefore}
import db.TestRepositoryGenerator.repositoryRowGen
import db.{RepositoryRow, TestRepositoryServiceImpl}
import spec.PostgresSuite

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.effect.PropF.forAllF

import java.time.LocalDateTime

final class RepositoryServiceImplSuite extends PostgresSuite:
  test("should find the repositories that have been updated the earliest"):
    forAllF(findTestCaseGen):
      case TestCase(rows, _, expected) =>
        testTransactor.resource.use: transactor =>
          val testRepositoryService = TestRepositoryServiceImpl(transactor)
          val repositoryService = RepositoryServiceImpl.Postgres(transactor)
          (for
            _ <- testRepositoryService.addAll(rows)
            obtained <- repositoryService.findEarliestUpdates()
          yield obtained).assertEquals(expected)

  test("should update the version of a repository"):
    forAllF(updateTestCaseGen):
      case TestCase(rows, maybeUpdate, expected) =>
        testTransactor.resource.use: transactor =>
          val testRepositoryService = TestRepositoryServiceImpl(transactor)
          val repositoryService = RepositoryServiceImpl.Postgres(transactor)
          (for
            update <- IO.fromOption(maybeUpdate)(
              IllegalArgumentException("Unsupported test case"),
            )
            (repository, version) = update
            _ <- testRepositoryService.addAll(rows)
            _ <- repositoryService.update(repository, version)
            obtained <- testRepositoryService.findBy(repository.id).value
          yield obtained).assertEquals(expected)

object RepositoryServiceImplSuite:
  final private case class TestCase[T, U](
      rows: NonEmptyList[RepositoryRow],
      update: Option[U],
      expected: T,
  )

  private val findTestCaseGen = for
    size <- Gen.choose(3, 5)
    currentTimestamp = LocalDateTime.now().nn
    frequencyInHours <- Gen.choose(1, 24)
    checkpoint = currentTimestamp.minusHours(frequencyInHours).nn
    earliestIds <- nDistinct(size, idGen)
    otherIds <- nDistinctExcluding(size, idGen, earliestIds)
    earliestProjects <- nDistinct(size, projectGen())
    otherProjects <- nDistinctExcluding(size, projectGen(), earliestProjects)
    earliestRows <- earliestIds.zip(earliestProjects).traverse { case (id, (groupId, artifactId)) =>
      repositoryRowGen(
        idGen = id.toLong,
        groupIdGen = groupId,
        artifactIdGen = artifactId,
        updatedAtGen = localDateTimeBefore(checkpoint).map(_.minusMinutes(3L).nn),
      )
    }
    otherRows <- otherIds.zip(otherProjects).traverse { case (id, (groupId, artifactId)) =>
      repositoryRowGen(
        idGen = id.toLong,
        groupIdGen = groupId,
        artifactIdGen = artifactId,
        updatedAtGen = localDateTimeAfter(checkpoint),
      )
    }
    expected = earliestRows.map(row =>
      Repository(
        Id.applyUnsafe(row.id),
        GroupId.applyUnsafe(row.groupId),
        ArtifactId.applyUnsafe(row.artifactId),
        Version.applyUnsafe(row.version),
      ),
    )
  yield TestCase(NonEmptyList.fromListUnsafe(earliestRows ++ otherRows), None, expected)

  private val updateTestCaseGen = for
    size <- Gen.choose(3, 5)
    ids <- nDistinct(size, idGen)
    projects <- nDistinct(size, projectGen())
    rows <- ids
      .zip(projects)
      .traverse { case (id, (groupId, artifactId)) =>
        repositoryRowGen(idGen = id.toLong, groupIdGen = groupId, artifactIdGen = artifactId)
      }
      .map(NonEmptyList.fromListUnsafe)
    selectedRepository =
      val selectedRow = rows.head
      Repository(
        Id.applyUnsafe(selectedRow.id),
        GroupId.applyUnsafe(selectedRow.groupId),
        ArtifactId.applyUnsafe(selectedRow.artifactId),
        Version.applyUnsafe(selectedRow.version),
      )
    newVersion <- versionGen.retryUntil(_ != selectedRepository.version)
    update = (selectedRepository, newVersion)
    expected = selectedRepository.copy(version = newVersion)
  yield TestCase(rows, Some(update), Some(expected))
