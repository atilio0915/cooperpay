package cooperpay.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class H2PagamentoStatusMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public H2PagamentoStatusMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE pagamento ALTER COLUMN status ENUM('RASCUNHO','PAGO','PENDENTE','FALHOU')");
        } catch (Exception ignored) {
            // Se o schema ja estiver correto (ou em outro banco), segue o startup.
        }
    }
}
