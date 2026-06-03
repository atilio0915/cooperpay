package cooperpay.server;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cooperpay.domain.Loja;
import cooperpay.domain.Motoboy;
import cooperpay.dto.LojaDTO;
import cooperpay.dto.LojaDTOResponse;
import cooperpay.repository.LojaRepository;
import cooperpay.repository.MotoboyRepository;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LojaService implements LojaInterface {

    private final LojaRepository lojaRepository;
    private final MotoboyRepository motoboyRepository;

    @Override
    public List<LojaDTOResponse> listar() {
        return lojaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LojaDTOResponse inserir(LojaDTO request) {
        Loja loja = new Loja();
        loja.setNome(request.getNome());

        Loja salva = lojaRepository.save(loja);
        return toResponse(salva);
    }

    @Override
    public void excluir(Long id) {
        Loja loja = lojaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Loja nao encontrada."));

        List<Motoboy> motoboys = motoboyRepository.findByLoja_Id(id);
        for (Motoboy motoboy : motoboys) {
            motoboy.setLoja(null);
        }
        motoboyRepository.saveAll(motoboys);

        lojaRepository.delete(loja);
    }

    private LojaDTOResponse toResponse(Loja loja) {
        LojaDTOResponse response = new LojaDTOResponse();
        response.setLojaId(loja.getId());
        response.setNome(loja.getNome());
        return response;
    }
}
