package cooperpay.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cooperpay.dto.MotoboyDTO;
import cooperpay.dto.MotoboyDTOResponse;
import cooperpay.server.MotoboyInterface;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/motoboy")
@RequiredArgsConstructor
public class MotobyController {

    private final MotoboyInterface motoboyService;

    @GetMapping
    public ResponseEntity<List<MotoboyDTOResponse>> buscarMotoboy(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long lojaId) {
        return ResponseEntity.ok(motoboyService.buscarPorNome(nome, lojaId));
    }

    @PostMapping
    public ResponseEntity<MotoboyDTOResponse> inserirMotoboy(
            @RequestBody MotoboyDTO request) {
        return ResponseEntity.ok(motoboyService.inserir(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotoboyDTOResponse> atualizarMotoboy(
            @PathVariable Long id,
            @RequestBody MotoboyDTO request) {
        return ResponseEntity.ok(motoboyService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMotoboy(@PathVariable Long id) {
        motoboyService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
