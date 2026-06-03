package cooperpay.server;

import java.math.BigDecimal;

public interface PixInterface {

    String enviarPix(String chavePix, BigDecimal valor, String idLocal);

    String consultarStatus(String idAsaas);
}
