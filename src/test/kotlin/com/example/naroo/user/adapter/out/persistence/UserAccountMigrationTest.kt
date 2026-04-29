package com.example.naroo.user.adapter.`out`.persistence

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.DriverManager

class UserAccountMigrationTest {
    @Test
    fun `migration creates user accounts table`() {
        val jdbcUrl = "jdbc:h2:mem:naroo-migration-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"

        Flyway.configure()
            .dataSource(jdbcUrl, "sa", "")
            .locations("classpath:db/migration")
            .load()
            .migrate()

        DriverManager.getConnection(jdbcUrl, "sa", "").use { connection ->
            connection.prepareStatement("select count(*) from user_accounts").use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    assertEquals(0, resultSet.getInt(1))
                }
            }
            connection.prepareStatement("select count(*) from diagnostic_starting_points").use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    assertEquals(0, resultSet.getInt(1))
                }
            }
        }
    }
}
