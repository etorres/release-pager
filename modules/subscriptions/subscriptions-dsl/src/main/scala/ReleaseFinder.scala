package es.eriktorr.pager

import cats.data.OptionT
import cats.effect.IO

trait ReleaseFinder[Repository, Version]:
  def findNewVersionOf(repository: Repository): OptionT[IO, Version]
