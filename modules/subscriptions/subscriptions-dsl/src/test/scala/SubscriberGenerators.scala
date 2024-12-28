package es.eriktorr.pager

import Subscriber.{ChatId, Name}
import commons.spec.StringGenerators.alphaLowerStringBetween

import org.scalacheck.Gen

object SubscriberGenerators:
  val chatIdGen: Gen[ChatId] = Gen.choose(1L, Long.MaxValue).map(ChatId.applyUnsafe)

  val nameGen: Gen[Name] = alphaLowerStringBetween(3, 7).map(Name.applyUnsafe)
