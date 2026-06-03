package cooperpay.server;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cooperpay.domain.Loja;
import cooperpay.domain.Motoboy;
import cooperpay.domain.Pagamento;
import cooperpay.dto.MotoboyDTO;
import cooperpay.dto.MotoboyDTOResponse;
import cooperpay.repository.LojaRepository;
import cooperpay.repository.MotoboyRepository;
import cooperpay.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MotoboyService implements MotoboyInterface {

    private final MotoboyRepository motoboyRepository;
    private final LojaRepository lojaRepository;
    private final PagamentoRepository pagamentoRepository;

    @Override
    public List<MotoboyDTOResponse> buscarPorNome(String nome, Long lojaId) {
        String filtroNome = nome == null ? "" : nome;

        List<Motoboy> motoboys = lojaId == null
                ? motoboyRepository.findByNomeStartingWithIgnoreCase(filtroNome)
                : motoboyRepository.findByNomeStartingWithIgnoreCaseAndLoja_Id(filtroNome, lojaId);

        return motoboys
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MotoboyDTOResponse inserir(MotoboyDTO request) {
        validarDuplicidade(request.getNome(), request.getPix(), null);

        Motoboy motoboy = new Motoboy();
        motoboy.setNome(request.getNome());
        motoboy.setPix(request.getPix());
        motoboy.setTrabalhou(false);
        motoboy.setLoja(buscarLoja(request.getLojaId()));

        Motoboy salvo = motoboyRepository.save(motoboy);
        return toResponse(salvo);
    }

    @Override
    @Transactional
    public MotoboyDTOResponse atualizar(Long id, MotoboyDTO request) {
        Motoboy motoboy = motoboyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Motoboy nao encontrado."));

        validarDuplicidade(request.getNome(), request.getPix(), id);

        motoboy.setNome(request.getNome());
        motoboy.setPix(request.getPix());
        if (request.getTrabalhou() != null) {
            motoboy.setTrabalhou(request.getTrabalhou());
        }
        if (request.getLojaId() != null) {
            Long lojaId = request.getLojaId();
            if (lojaId <= 0) {
                motoboy.setLoja(null);
            } else {
                motoboy.setLoja(buscarLoja(lojaId));
            }
        }

        Motoboy salvo = motoboyRepository.save(motoboy);
        return toResponse(salvo);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Motoboy motoboy = motoboyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Motoboy nao encontrado."));

        // Mantém o histórico: desvincula o motoboy dos pagamentos antes de excluí-lo
        List<Pagamento> pagamentos = pagamentoRepository.filtrar(id, null, null);
        for (Pagamento p : pagamentos) {
            p.setMotoboy(null);
        }
        pagamentoRepository.saveAll(pagamentos);

        motoboyRepository.delete(motoboy);
    }

    private MotoboyDTOResponse toResponse(Motoboy motoboy) {
        MotoboyDTOResponse dto = new MotoboyDTOResponse();
        dto.setMotoboyId(motoboy.getId());
        dto.setNome(motoboy.getNome());
        dto.setPix(motoboy.getPix());
        dto.setTrabalhou(Boolean.TRUE.equals(motoboy.getTrabalhou()));
        if (motoboy.getLoja() != null) {
            dto.setLojaId(motoboy.getLoja().getId());
            dto.setLojaNome(motoboy.getLoja().getNome());
        }
        return dto;
    }

    private Loja buscarLoja(Long lojaId) {
        if (lojaId == null || lojaId <= 0) {
            return null;
        }
        return lojaRepository.findById(lojaId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Loja nao encontrada."));
    }

    private void validarDuplicidade(String nome, String pix, Long idAtual) {
        boolean existe = idAtual == null
                ? motoboyRepository.existsByNomeIgnoreCaseAndPixIgnoreCase(nome, pix)
                : motoboyRepository.existsByNomeIgnoreCaseAndPixIgnoreCaseAndIdNot(nome, pix, idAtual);

        if (existe) {
            throw new ResponseStatusException(CONFLICT, "Esse motoboy ja existe.");
        }
    }
}
