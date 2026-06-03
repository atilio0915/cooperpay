package cooperpay.server;

import java.util.List;

import cooperpay.dto.MotoboyDTO;
import cooperpay.dto.MotoboyDTOResponse;

public interface MotoboyInterface {

    List<MotoboyDTOResponse> buscarPorNome(String nome, Long lojaId);

    MotoboyDTOResponse inserir(MotoboyDTO request);

    MotoboyDTOResponse atualizar(Long id, MotoboyDTO request);

    void excluir(Long id);
}
