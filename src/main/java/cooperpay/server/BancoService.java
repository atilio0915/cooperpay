package cooperpay.server;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("mock")
@Service
public class BancoService implements PixInterface {

    @Override
    public String enviarPix(String chavePix, BigDecimal valor, String idLocal) {
        return "pix-" + UUID.randomUUID();
    }

    @Override
    public String consultarStatus(String idAsaas) {
        return "DONE";
    }
}
