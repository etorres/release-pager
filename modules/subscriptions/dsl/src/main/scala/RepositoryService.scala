package es.eriktorr.pager

import cats.effect.IO

trait RepositoryService[Repository, Updated, Version]:
  def findEarliestUpdates(): IO[List[Repository]]

  def update(repository: Repository, version: Version): IO[Updated]
