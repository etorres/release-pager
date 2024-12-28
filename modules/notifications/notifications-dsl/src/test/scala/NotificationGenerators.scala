package es.eriktorr.pager

import Notification.*
import commons.spec.CollectionGenerators.nDistinct
import commons.spec.StringGenerators.alphaLowerStringBetween

import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.genInstances

object NotificationGenerators:
  val chatIdGen: Gen[ChatId] = Gen.choose(1L, Long.MaxValue).map(ChatId.applyUnsafe)

  val nameGen: Gen[Name] = alphaLowerStringBetween(3, 7).map(Name.applyUnsafe)

  val addresseesGen: Gen[List[Addressee]] = for
    size <- Gen.choose(1, 3)
    chatIds <- nDistinct(size, chatIdGen)
    addressees <- chatIds.traverse(chatId => nameGen.map(Addressee(chatId, _)))
  yield addressees

  val projectNameGen: Gen[ProjectName] = alphaLowerStringBetween(3, 7).map(ProjectName.applyUnsafe)

  val versionGen: Gen[Version] = alphaLowerStringBetween(3, 7).map(Version.applyUnsafe)

  def notificationGen(
      addresseesGen: Gen[List[Addressee]] = addresseesGen,
      projectNameGen: Gen[ProjectName] = projectNameGen,
      versionGen: Gen[Version] = versionGen,
  ): Gen[Notification] = for
    addressees <- addresseesGen
    projectName <- projectNameGen
    version <- versionGen
  yield Notification(addressees, projectName, version)
