CREATE TABLE repositories (
    id BIGINT NOT NULL PRIMARY KEY,
    group_id TEXT NOT NULL,
    artifact_id TEXT NOT NULL,
    version TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, artifact_id)
);

CREATE TABLE subscribers (
    id BIGINT NOT NULL PRIMARY KEY,
    chat_id TEXT NOT NULL
        CONSTRAINT chat_ids_must_be_different UNIQUE,
    name TEXT NOT NULL
);

CREATE TABLE subscriptions (
    repository_id BIGINT NOT NULL
        REFERENCES repositories(id)
        ON UPDATE CASCADE,
    subscriber_id BIGINT NOT NULL
        REFERENCES subscribers(id)
        ON UPDATE CASCADE,
    PRIMARY KEY (repository_id, subscriber_id)
);
