package cooperpay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotoboyDTOResponse {

    private Long motoboyId;
    private String nome;
    private String pix;
    private Boolean trabalhou;
    private Long lojaId;
    private String lojaNome;
}
