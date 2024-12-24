package es.eriktorr.pager

import Repository.{ArtifactId, GroupId, Id, Version}
import RepositoryGenerators.projectGen
import RepositoryServiceImplSuite.{testCaseGen, TestCase}
import SubscriptionGenerator.idGen
import commons.spec.CollectionGenerators.{nDistinct, nDistinctExcluding}
import commons.spec.TemporalGenerators.{localDateTimeAfter, localDateTimeBefore}
import db.TestRepositoryGenerator.repositoryRowGen
import db.{RepositoryRow, TestRepositoryServiceImpl}
import spec.PostgresSuite

import cats.data.NonEmptyList
import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.effect.PropF.forAllF

import java.time.LocalDateTime

final class RepositoryServiceImplSuite extends PostgresSuite:
  test("should find the repositories that have been updated the earliest"):
    forAllF(testCaseGen):
      case TestCase(rows, expected) =>
        testTransactor.resource.use: transactor =>
          val testRepositoryService = TestRepositoryServiceImpl(transactor)
          val repositoryService = RepositoryServiceImpl.Postgres(transactor)
          (for
            _ <- testRepositoryService.addAll(rows)
            obtained <- repositoryService.findEarliestUpdates()
          yield obtained).assertEquals(expected)

object RepositoryServiceImplSuite:
  final private case class TestCase(rows: NonEmptyList[RepositoryRow], expected: List[Repository])

  private val testCaseGen = for
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
  yield TestCase(NonEmptyList.fromListUnsafe(earliestRows ++ otherRows), expected)
