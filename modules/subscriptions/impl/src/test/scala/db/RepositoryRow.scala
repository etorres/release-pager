package es.eriktorr.pager
package db

import java.time.LocalDateTime

final case class RepositoryRow(
    id: Long,
    groupId: String,
    artifactId: String,
    version: String,
    updatedAt: LocalDateTime,
)
