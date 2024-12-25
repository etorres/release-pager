package es.eriktorr.pager

import cats.data.OptionT
import cats.effect.IO

final class FakeReleaseFinder(filter: Int => Boolean) extends ReleaseFinder[Int, String]:
  override def findNewVersionOf(repository: Int): OptionT[IO, String] =
    if filter(repository) then OptionT.pure(s"released->{repository:$repository}") else OptionT.none
