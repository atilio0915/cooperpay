package cooperpay.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cooperpay.dto.LojaDTO;
import cooperpay.dto.LojaDTOResponse;
import cooperpay.server.LojaInterface;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaInterface lojaService;

    @GetMapping
    public ResponseEntity<List<LojaDTOResponse>> listar() {
        return ResponseEntity.ok(lojaService.listar());
    }

    @PostMapping
    public ResponseEntity<LojaDTOResponse> inserir(@RequestBody LojaDTO request) {
        return ResponseEntity.ok(lojaService.inserir(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        lojaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
