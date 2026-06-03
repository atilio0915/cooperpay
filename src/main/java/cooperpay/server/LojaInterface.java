package cooperpay.server;

import java.util.List;

import cooperpay.dto.LojaDTO;
import cooperpay.dto.LojaDTOResponse;

public interface LojaInterface {
    List<LojaDTOResponse> listar();

    LojaDTOResponse inserir(LojaDTO request);

    void excluir(Long id);
}
