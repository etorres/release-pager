package es.eriktorr.pager

import RepositoryGenerators.projectGen
import SubscriberGenerators.nameGen
import SubscriptionGenerator.idGen
import SubscriptionServiceImplSuite.{testCaseGen, TestCase}
import commons.spec.CollectionGenerators.{nDistinct, nDistinctExcluding}
import db.TestRepositoryGenerator.repositoryRowGen
import db.TestSubscriptionGenerator.subscriberRowGen
import db.{RepositoryRow, SubscriberRow, TestRepositoryServiceImpl, TestSubscriptionServiceImpl}
import spec.PostgresSuite

import cats.data.NonEmptyList
import cats.implicits.{catsKernelOrderingForOrder, toFoldableOps, toTraverseOps}
import io.hypersistence.tsid.TSID
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.effect.PropF.forAllF

final class SubscriptionServiceImplSuite extends PostgresSuite:
  test("should find all subscribers of a repository"):
    forAllF(testCaseGen):
      case TestCase(repositoryRows, subscriberRows, subscriptions, selectedRepository, expected) =>
        testTransactor.resource.use: transactor =>
          val testRepositoryService = TestRepositoryServiceImpl(transactor)
          val testSubscriptionService = TestSubscriptionServiceImpl(transactor)
          val subscriptionService = SubscriptionServiceImpl.Postgres(transactor)
          (for
            _ <- testRepositoryService.addAll(repositoryRows)
            _ <- testSubscriptionService.addAll(subscriberRows)
            _ <- subscriptions.traverse_ { case (repository, subscriberId) =>
              subscriptionService.subscribe(repository, subscriberId)
            }
            obtained <- subscriptionService.subscribersOf(selectedRepository)
          yield obtained.sorted).assertEquals(expected.sorted)

object SubscriptionServiceImplSuite:
  final private case class TestCase(
      repositoryRows: NonEmptyList[RepositoryRow],
      subscriberRows: NonEmptyList[SubscriberRow],
      subscriptions: NonEmptyList[(Repository, Subscriber.Id)],
      repository: Repository,
      expected: List[Subscriber],
  )

  private val testCaseGen = for
    size <- Gen.choose(3, 5)
    selectedRepositoryId <- idGen
    selectedProject <- projectGen()
    otherRepositoryIds <- nDistinctExcluding(size, idGen, List(selectedRepositoryId))
    otherProjects <- nDistinctExcluding(size, projectGen(), List(selectedProject))
    selectedRepositoryRow <- repositoryRowGen(
      idGen = selectedRepositoryId.toLong,
      groupIdGen = selectedProject._1,
      artifactIdGen = selectedProject._2,
    )
    otherRepositoryRows <- otherRepositoryIds.zip(otherProjects).traverse {
      case (id, (groupId, artifactId)) =>
        repositoryRowGen(
          idGen = id.toLong,
          groupIdGen = groupId,
          artifactIdGen = artifactId,
        )
    }
    subscriberIds <- nDistinct(size, idGen)
    subscriberNames <- nDistinct(size, nameGen)
    noSubscriberIds <- nDistinctExcluding(size, idGen, subscriberIds)
    noSubscriberNames <- nDistinctExcluding(size, nameGen, subscriberNames)
    subscriberRows <- subscriberIds.zip(subscriberNames).traverse { case (id, name) =>
      subscriberRowGen(idGen = id.toLong, nameGen = name)
    }
    noSubscriberRows <- noSubscriberIds.zip(noSubscriberNames).traverse { case (id, name) =>
      subscriberRowGen(idGen = id.toLong, nameGen = name)
    }
    selectedRepository = Repository(
      Repository.Id.applyUnsafe(selectedRepositoryRow.id),
      Repository.GroupId.applyUnsafe(selectedRepositoryRow.groupId),
      Repository.ArtifactId.applyUnsafe(selectedRepositoryRow.artifactId),
      Repository.Version.applyUnsafe(selectedRepositoryRow.version),
    )
    otherRepositories = otherRepositoryRows.map(repositoryRow =>
      Repository(
        Repository.Id.applyUnsafe(repositoryRow.id),
        Repository.GroupId.applyUnsafe(repositoryRow.groupId),
        Repository.ArtifactId.applyUnsafe(repositoryRow.artifactId),
        Repository.Version.applyUnsafe(repositoryRow.version),
      ),
    )
    selectedSubscriptions = subscriberIds.map(id =>
      selectedRepository -> Subscriber.Id.applyUnsafe(id.toLong),
    )
    otherSubscriptions <- otherRepositories.traverse(repository =>
      Gen
        .oneOf(subscriberIds ++ noSubscriberIds)
        .map(subscriberId => repository -> Subscriber.Id.applyUnsafe(subscriberId.toLong)),
    )
    subscribers = subscriberRows.map(subscriberRow =>
      Subscriber(
        Subscriber.Id.applyUnsafe(subscriberRow.id),
        Subscriber.ChatId.applyUnsafe(subscriberRow.chatId),
        Subscriber.Name.applyUnsafe(subscriberRow.name),
      ),
    )
  yield TestCase(
    repositoryRows = NonEmptyList.of(selectedRepositoryRow, otherRepositoryRows*),
    subscriberRows = NonEmptyList.fromListUnsafe(subscriberRows ++ noSubscriberRows),
    subscriptions = NonEmptyList.fromListUnsafe(selectedSubscriptions ++ otherSubscriptions),
    repository = selectedRepository,
    expected = subscribers,
  )
