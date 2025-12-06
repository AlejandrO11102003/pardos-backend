package com.pardos.pos.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SequenceFixConfig {

    @Bean
    public ApplicationRunner fixSequences(JdbcTemplate jdbcTemplate) {
        return args -> {
            String[] tables = new String[]{"products", "tables", "orders", "order_items", "sales"};
            for (String table : tables) {
                try {
                    String sql = String.format(
                        "SELECT setval(pg_get_serial_sequence('%s', 'id'), COALESCE((SELECT MAX(id) FROM %s),0) + 1, false);",
                        table, table);
                    jdbcTemplate.execute(sql);
                } catch (Exception ex) {
                    System.out.println("Sequence fix failed for table '" + table + "': " + ex.getMessage());
                }
            }
        };
    }
}
