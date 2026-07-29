package com.dbtraining.reconx.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV079 — LiquibaseMigrationsIT integration test.
 * Verifies that Liquibase applies all expected database changeSets.
 */
@SpringBootTest
@ActiveProfiles("dev")
class LiquibaseMigrationsIT {

    @Autowired JdbcTemplate jdbc;

    @Test
    void liquibase_applied_all_expected_changesets() {
        Integer applied = jdbc.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog", Integer.class);
        assertThat(applied).isGreaterThanOrEqualTo(13);

        Integer counterparties = jdbc.queryForObject(
                "SELECT COUNT(*) FROM counterparties", Integer.class);
        assertThat(counterparties).isGreaterThanOrEqualTo(10);
    }
}
