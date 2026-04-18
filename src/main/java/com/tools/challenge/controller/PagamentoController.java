package com.tools.challenge.controller;

import com.tools.challenge.dto.PagamentoRequest;
import com.tools.challenge.dto.TransacaoResponse;
import com.tools.challenge.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/pagamento")
public class PagamentoController {

    private final TransacaoService service;

    public PagamentoController(TransacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse.Raiz> pagar(
            @Valid @RequestBody PagamentoRequest.Raiz request) {

        TransacaoResponse.Raiz response = service.realizarPagamento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/estorno")
    public ResponseEntity<TransacaoResponse.Raiz> estornar(@PathVariable String id) {
        return ResponseEntity.ok(service.estornar(id));
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponse.Raiz>> consultarTodas() {
        return ResponseEntity.ok(service.consultarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponse.Raiz> consultarPorId(@PathVariable String id) {
        return ResponseEntity.ok(service.consultarPorId(id));
    }
}