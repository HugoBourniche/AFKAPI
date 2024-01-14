DROP TABLE fights;
DROP TABLE teams;
DROP TABLE characters;
DROP TABLE factions;

CREATE TABLE version (
    version     VARCHAR(255) PRIMARY KEY NOT NULL
);

CREATE TABLE factions (
    name        VARCHAR(255) PRIMARY KEY NOT NULL,
    weakness    VARCHAR(255)                 NULL,
    strongness  VARCHAR(255)                 NULL
);

CREATE TABLE characters (
    name          VARCHAR(255) PRIMARY KEY   NOT NULL,
    full_name     VARCHAR(255)               NOT NULL,
    recap         VARCHAR(255)               NOT NULL,
    faction_name  VARCHAR(255)               NOT NULL,
    type          VARCHAR(255)               NOT NULL,
    classe        VARCHAR(255)               NOT NULL,
    role          VARCHAR(255)               NOT NULL,
    rank          INTEGER                     NOT NULL,
    FOREIGN KEY (faction_name) REFERENCES factions(name)
);

CREATE TABLE teams (
    id uuid          DEFAULT uuid_generate_v1mc(),
    character_1_name VARCHAR(255) NOT NULL,
    character_2_name VARCHAR(255) NOT NULL,
    character_3_name VARCHAR(255) NOT NULL,
    character_4_name VARCHAR(255) NOT NULL,
    character_5_name VARCHAR(255) NOT NULL,
    use              INTEGER      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (character_1_name) REFERENCES characters(name),
    FOREIGN KEY (character_2_name) REFERENCES characters(name),
    FOREIGN KEY (character_3_name) REFERENCES characters(name),
    FOREIGN KEY (character_4_name) REFERENCES characters(name),
    FOREIGN KEY (character_5_name) REFERENCES characters(name)
);

CREATE TABLE fights (
    winner_id uuid,
    loser_id  uuid,
    PRIMARY KEY (winner_id, loser_id),
    FOREIGN KEY (winner_id) REFERENCES teams(id),
    FOREIGN KEY (loser_id) REFERENCES teams(id)
);