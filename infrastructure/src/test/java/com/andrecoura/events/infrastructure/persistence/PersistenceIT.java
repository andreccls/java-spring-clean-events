package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.infrastructure.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Base class: real MySQL, real Flyway migrations, empty tables before each test. */
@SpringBootTest(classes = TestApplication.class)
abstract class PersistenceIT {

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void emptyTables() {
        jdbc.execute("DELETE FROM registrations");
        jdbc.execute("DELETE FROM participants");
        jdbc.execute("DELETE FROM events");
    }
}
