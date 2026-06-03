package cooperpay.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumStatus {
    RASCUNHO("Rascunho"),
    PAGO("Pago"),
    PENDENTE("Pendente"),
    FALHOU("Falhou");

    private final String descricao;
}
