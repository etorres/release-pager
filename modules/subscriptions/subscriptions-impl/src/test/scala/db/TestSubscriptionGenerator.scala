package es.eriktorr.pager
package db

import org.scalacheck.Gen

object TestSubscriptionGenerator:
  def subscriberRowGen(
      idGen: Gen[Long] = SubscriptionGenerator.idGen.map(_.toLong),
      chatIdGen: Gen[Long] = SubscriberGenerators.chatIdGen,
      nameGen: Gen[String] = SubscriberGenerators.nameGen,
  ): Gen[SubscriberRow] = for
    id <- idGen
    chatId <- chatIdGen
    name <- nameGen
  yield SubscriberRow(id, chatId, name)
