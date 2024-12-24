package es.eriktorr.pager

import io.hypersistence.tsid.TSID
import org.scalacheck.Gen

object SubscriptionGenerator:
  val idGen: Gen[TSID] = Gen.choose(1L, Long.MaxValue).map(TSID.from(_).nn)
