CREATE TABLE events (
    id             CHAR(36)      NOT NULL,
    title          VARCHAR(200)  NOT NULL,
    description    VARCHAR(2000) NULL,
    venue_name     VARCHAR(120)  NOT NULL,
    venue_address  VARCHAR(255)  NOT NULL,
    starts_at      DATETIME(6)   NOT NULL,
    ends_at        DATETIME(6)   NOT NULL,
    capacity       INT           NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_events_dates CHECK (ends_at > starts_at),
    CONSTRAINT ck_events_capacity CHECK (capacity >= 0),
    INDEX ix_events_status_starts_at (status, starts_at),
    INDEX ix_events_starts_at (starts_at)
);

CREATE TABLE participants (
    id     CHAR(36)     NOT NULL,
    name   VARCHAR(120) NOT NULL,
    email  VARCHAR(254) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_participants_email UNIQUE (email)
);

CREATE TABLE registrations (
    id              CHAR(36)    NOT NULL,
    event_id        CHAR(36)    NOT NULL,
    participant_id  CHAR(36)    NOT NULL,
    status          VARCHAR(20) NOT NULL,
    registered_at   DATETIME(6) NOT NULL,
    cancelled_at    DATETIME(6) NULL,
    -- 1 while ACTIVE, NULL otherwise. MySQL unique indexes ignore NULLs, so this enforces
    -- "one ACTIVE registration per participant per event" while keeping cancelled rows as history.
    active_key      TINYINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN 1 ELSE NULL END) STORED,
    PRIMARY KEY (id),
    CONSTRAINT uq_registrations_one_active UNIQUE (event_id, participant_id, active_key),
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_registrations_participant FOREIGN KEY (participant_id) REFERENCES participants (id),
    INDEX ix_registrations_participant (participant_id)
);
