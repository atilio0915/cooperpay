package cooperpay.server;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import cooperpay.domain.EnumStatus;
import cooperpay.domain.Motoboy;
import cooperpay.domain.Pagamento;
import cooperpay.dto.PagamentoDTOResponse;
import cooperpay.dto.PagamentoFiltroDto;
import cooperpay.repository.MotoboyRepository;
import cooperpay.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoServe implements PagamentoInterface {

    private final MotoboyRepository motoboyRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PixInterface pixService;

    @Value("${asaas.api.access-token}")
    private String accessToken;

    @Override
    public Pagamento realizarPagamento(Long idMotoboy, BigDecimal valor, String semanaReferencia) {
        System.out.println("[SERVICE] Iniciando realizarPagamento - Motoboy ID: " + idMotoboy + ", Valor: " + valor + ", Semana: " + semanaReferencia);
        
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Token do Asaas nao configurado. Crie a variavel de ambiente ASAAS_TOKEN.");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("[SERVICE] Erro: Valor de pagamento invalido: " + valor);
            throw new IllegalArgumentException("Valor invalido para pagamento");
        }

        Motoboy motoboy = motoboyRepository.findById(idMotoboy)
                .orElseThrow(() -> { System.err.println("[SERVICE] Erro: Motoboy " + idMotoboy + " nao encontrado"); return new IllegalArgumentException("Motoboy nao encontrado"); });

        Pagamento pagamento = new Pagamento();
        pagamento.setMotoboy(motoboy);
        pagamento.setValor(valor);
        pagamento.setSemanaReferencia(semanaReferencia);
        pagamento.setStatus(EnumStatus.PENDENTE);
        pagamento.setData(LocalDateTime.now());
        pagamento.setSalvo(false);

        pagamentoRepository.save(pagamento);
        System.out.println("[SERVICE] Registro de pagamento PENDENTE criado no banco. ID: " + pagamento.getIdPagamento());
        
        // Ao integrar com Asaas, garantimos que o erro na API não trave o banco de dados
        try {
            System.out.println("[SERVICE] Chamando integracao Pix para " + motoboy.getNome() + " (" + motoboy.getPix() + ")");
            String txidPix = pixService.enviarPix(motoboy.getPix(), valor, pagamento.getIdPagamento().toString());
            pagamento.setStatus(EnumStatus.PENDENTE);
            pagamento.setTrasactinIdPix(txidPix);
            pagamento.setSalvo(true);
            System.out.println("[SERVICE] Sucesso! Pix enviado. TXID: " + txidPix);
        } catch (Exception e) {
            // Log detalhado para depuração no console
            System.err.println("[ASAAS ERROR] Falha no pagamento para " + motoboy.getNome() + ": " + e.getMessage());
            pagamento.setStatus(EnumStatus.FALHOU);
            pagamento.setSalvo(false);
        }

        return pagamentoRepository.save(pagamento);
    }

    @Override
    public List<PagamentoDTOResponse> filtrar(PagamentoFiltroDto filtro) {
        Long idMotoboy = filtro != null ? filtro.getIdMotoboy() : null;
        String semana = filtro != null ? filtro.getSemanaReferencia() : null;
        EnumStatus status = filtro != null ? filtro.getStatus() : null;
        LocalDate dataPagamento = filtro != null ? filtro.getDataPagamento() : null;

        return pagamentoRepository.filtrar(idMotoboy, semana, status)
                .stream()
                .filter(pagamento -> dataPagamento == null
                        || (pagamento.getData() != null && pagamento.getData().toLocalDate().isEqual(dataPagamento)))
                .map(this::toDto)
                .toList();
    }

    @Override
    public Pagamento criarRascunho(Long idMotoboy, String semanaReferencia) {
        Motoboy motoboy = motoboyRepository.findById(idMotoboy)
                .orElseThrow(() -> new IllegalArgumentException("Motoboy nao encontrado"));

        return pagamentoRepository.findByMotoboyIdAndSemanaReferenciaAndStatus(idMotoboy, semanaReferencia, EnumStatus.RASCUNHO)
                .orElseGet(() -> {
                    Pagamento pagamento = new Pagamento();
                    pagamento.setMotoboy(motoboy);
                    pagamento.setValor(BigDecimal.ZERO);
                    pagamento.setSemanaReferencia(semanaReferencia);
                    pagamento.setStatus(EnumStatus.RASCUNHO);
                    pagamento.setData(LocalDateTime.now());
                    pagamento.setSalvo(false);
                    return pagamentoRepository.save(pagamento);
                });
    }

    @Override
    public void removerRascunho(Long idMotoboy, String semanaReferencia) {
        pagamentoRepository.findByMotoboyIdAndSemanaReferenciaAndStatus(idMotoboy, semanaReferencia, EnumStatus.RASCUNHO)
                .ifPresent(pagamentoRepository::delete);
    }

    @Override
    public Pagamento atualizarValorRascunho(Long idPagamento, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor invalido para pagamento");
        }

        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento nao encontrado"));

        if (pagamento.getStatus() != EnumStatus.RASCUNHO) {
            throw new IllegalArgumentException("Somente pagamento rascunho pode ter valor alterado");
        }

        pagamento.setValor(valor);
        return pagamentoRepository.save(pagamento);
    }

    @Override
    public Pagamento pagarRascunho(Long idPagamento) {
        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento nao encontrado"));

        if (pagamento.getStatus() != EnumStatus.RASCUNHO) {
            throw new IllegalArgumentException("Somente pagamento rascunho pode ser pago");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Token do Asaas nao configurado. Crie a variavel de ambiente ASAAS_TOKEN.");
        }

        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("[SERVICE] Erro: Rascunho " + idPagamento + " esta com valor zero ou nulo.");
            throw new IllegalArgumentException("Informe um valor maior que zero para pagar");
        }

        System.out.println("[DEBUG - Pagamento] Iniciando processamento de rascunho ID: " + idPagamento);

        // Definimos como PENDENTE antes de chamar a API para o usuário ver que está em processamento
        pagamento.setStatus(EnumStatus.PENDENTE);
        pagamentoRepository.save(pagamento);

        try {
            String txidPix = pixService.enviarPix(pagamento.getMotoboy().getPix(), pagamento.getValor(), pagamento.getIdPagamento().toString());
            pagamento.setTrasactinIdPix(txidPix);
            pagamento.setStatus(EnumStatus.PENDENTE);
            pagamento.setSalvo(true);
            System.out.println("[DEBUG - Pagamento] Sucesso! TXID: " + txidPix);
        } catch (Exception e) {
            System.err.println("[DEBUG - Pagamento] ERRO CRITICO na API Asaas para o rascunho " + idPagamento + ": " + e.getMessage());
            pagamento.setStatus(EnumStatus.FALHOU);
            pagamento.setSalvo(false);
        }
        pagamento.setData(LocalDateTime.now());

        return pagamentoRepository.save(pagamento);
    }

    /**
     * TAREFA AGENDADA (POLLING)
     * Verifica a cada 30 segundos se os pagamentos pendentes foram concluídos no Asaas.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void verificarStatusPagamentosPendentes() {
        List<Pagamento> pendentes = pagamentoRepository.filtrar(null, null, EnumStatus.PENDENTE);
        
        for (Pagamento p : pendentes) {
            if (p.getTrasactinIdPix() == null) continue;

            String statusAsaas = pixService.consultarStatus(p.getTrasactinIdPix());
            System.out.println("[POLLING] Checando ID " + p.getIdPagamento() + " | Status Asaas: " + statusAsaas);

            if ("DONE".equals(statusAsaas)) {
                p.setStatus(EnumStatus.PAGO);
                p.setSalvo(true);
                pagamentoRepository.save(p);
                System.out.println("[POLLING] Pagamento " + p.getIdPagamento() + " confirmado como PAGO.");
            } else if ("FAILED".equals(statusAsaas) || "CANCELLED".equals(statusAsaas)) {
                p.setStatus(EnumStatus.FALHOU);
                p.setSalvo(false);
                pagamentoRepository.save(p);
                System.err.println("[POLLING] Pagamento " + p.getIdPagamento() + " FALHOU no Asaas.");
            }
        }
    }

    private PagamentoDTOResponse toDto(Pagamento pagamento) {
        return new PagamentoDTOResponse(
                pagamento.getIdPagamento(),
                pagamento.getMotoboy() != null ? pagamento.getMotoboy().getId() : null,
                pagamento.getMotoboy() != null ? pagamento.getMotoboy().getNome() : null,
                pagamento.getMotoboy() != null ? pagamento.getMotoboy().getPix() : null,
                pagamento.getMotoboy() != null ? pagamento.getMotoboy().getTrabalhou() : null,
                pagamento.getValor(),
                pagamento.getSemanaReferencia(),
                pagamento.getStatus() != null ? pagamento.getStatus().getDescricao() : null,
                pagamento.getTrasactinIdPix(),
                pagamento.getSalvo());
    }

    /**
     * Remove automaticamente pagamentos com mais de 30 dias.
     * O cron "0 0 3 * * SUN" executa a tarefa todo domingo às 03:00 da manhã.
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void limparHistoricoAntigo() {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        List<Pagamento> paraRemover = pagamentoRepository.filtrar(null, null, null).stream()
                .filter(p -> p.getData() != null && p.getData().isBefore(limite))
                .toList();

        if (!paraRemover.isEmpty()) {
            pagamentoRepository.deleteAll(paraRemover);
            System.out.println("[CLEANUP] Removidos " + paraRemover.size() + " registros de pagamentos antigos.");
        }
    }
}
