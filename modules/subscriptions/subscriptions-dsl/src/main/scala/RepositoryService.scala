package es.eriktorr.pager

import cats.effect.IO

trait RepositoryService[Repository, Version, Updated]:
  def findEarliestUpdates(): IO[List[Repository]]

  def update(repository: Repository, version: Version): IO[Updated]
