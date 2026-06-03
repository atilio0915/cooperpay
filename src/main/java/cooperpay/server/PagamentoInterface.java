package cooperpay.server;

import java.math.BigDecimal;
import java.util.List;

import cooperpay.domain.Pagamento;
import cooperpay.dto.PagamentoDTOResponse;
import cooperpay.dto.PagamentoFiltroDto;

public interface PagamentoInterface {

    Pagamento realizarPagamento(Long idMotoboy, BigDecimal valor, String semanaReferencia);

    List<PagamentoDTOResponse> filtrar(PagamentoFiltroDto filtro);

    Pagamento criarRascunho(Long idMotoboy, String semanaReferencia);

    void removerRascunho(Long idMotoboy, String semanaReferencia);

    Pagamento atualizarValorRascunho(Long idPagamento, BigDecimal valor);

    Pagamento pagarRascunho(Long idPagamento);
}
