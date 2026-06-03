package cooperpay.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cooperpay.domain.EnumStatus;
import cooperpay.domain.Pagamento;
import cooperpay.dto.PagamentoDTO;
import cooperpay.dto.PagamentoDTOResponse;
import cooperpay.dto.PagamentoFiltroDto;
import cooperpay.server.PagamentoInterface;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoInterface pagamentoService;

    @PostMapping
    public ResponseEntity<PagamentoDTOResponse> pagar(
            @RequestBody PagamentoDTO request) {
        Pagamento pagamento = pagamentoService.realizarPagamento(
                request.getIdMotoboy(),
                request.getValor(),
                request.getSemanaReferencia());

        PagamentoDTOResponse response = new PagamentoDTOResponse(
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

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PagamentoDTOResponse>> filtrar(
            @RequestParam(required = false) Long motoboyId,
            @RequestParam(required = false) String semanaReferencia,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        PagamentoFiltroDto filtro = new PagamentoFiltroDto();
        filtro.setIdMotoboy(motoboyId);
        filtro.setSemanaReferencia(semanaReferencia);
        filtro.setDataPagamento(data);

        if (status != null && !status.isBlank()) {
            filtro.setStatus(EnumStatus.valueOf(status.toUpperCase()));
        }

        return ResponseEntity.ok(pagamentoService.filtrar(filtro));
    }

    @PostMapping("/rascunho")
    public ResponseEntity<PagamentoDTOResponse> criarRascunho(@RequestBody PagamentoDTO request) {
        Pagamento pagamento = pagamentoService.criarRascunho(request.getIdMotoboy(), request.getSemanaReferencia());
        PagamentoDTOResponse response = new PagamentoDTOResponse(
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

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rascunho")
    public ResponseEntity<Void> removerRascunho(
            @RequestParam Long motoboyId,
            @RequestParam String semanaReferencia) {
        pagamentoService.removerRascunho(motoboyId, semanaReferencia);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idPagamento}/valor")
    public ResponseEntity<PagamentoDTOResponse> atualizarValorRascunho(
            @PathVariable Long idPagamento,
            @RequestParam java.math.BigDecimal valor) {
        Pagamento pagamento = pagamentoService.atualizarValorRascunho(idPagamento, valor);
        PagamentoDTOResponse response = new PagamentoDTOResponse(
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
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{idPagamento}/pagar")
    public ResponseEntity<PagamentoDTOResponse> pagarRascunho(@PathVariable Long idPagamento) {
        Pagamento pagamento = pagamentoService.pagarRascunho(idPagamento);
        PagamentoDTOResponse response = new PagamentoDTOResponse(
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
        return ResponseEntity.ok(response);
    }
}
