package es.eriktorr.pager

import cats.effect.IO

final class FakeRepositoryService(repositories: List[Int])
    extends RepositoryService[Int, String, String]:
  override def findEarliestUpdates(): IO[List[Int]] = IO.pure(repositories)

  override def update(repository: Int, version: String): IO[String] =
    IO.pure(s"updated->{repository:$repository,version:$version}")
