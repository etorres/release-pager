package es.eriktorr.pager
package db

import doobie.Meta

trait RepositoryMapper:
  given Meta[Repository.Id] = Meta[Long].tiemap(Repository.Id.either)(_.value)

  given Meta[Repository.GroupId] = Meta[String].tiemap(Repository.GroupId.either)(_.value)

  given Meta[Repository.ArtifactId] = Meta[String].tiemap(Repository.ArtifactId.either)(_.value)

  given Meta[Repository.Version] = Meta[String].tiemap(Repository.Version.either)(_.value)

object RepositoryMapper extends RepositoryMapper
