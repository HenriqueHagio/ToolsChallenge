package com.tools.challenge.handler;

import com.tools.challenge.exception.TransacaoDuplicadaException;
import com.tools.challenge.exception.TransacaoJaCanceladaException;
import com.tools.challenge.exception.TransacaoNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(TransacaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrada(TransacaoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(TransacaoDuplicadaException.class)
    public ResponseEntity<ErroResponse> handleDuplicada(TransacaoDuplicadaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErroResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    @ExceptionHandler(TransacaoJaCanceladaException.class)
    public ResponseEntity<ErroResponse> handleJaCancelada(TransacaoJaCanceladaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErroResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            campos.put(campo, error.getDefaultMessage());
        });

        ErroResponse body = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Dados de entrada inválidos",
                campos
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErroResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponse.of(500, "Erro interno: " + ex.getMessage()));
    }


    public record ErroResponse(
            int status,
            String mensagem,
            Map<String, String> campos,
            LocalDateTime timestamp
    ) {
        static ErroResponse of(int status, String mensagem) {
            return new ErroResponse(status, mensagem, null, LocalDateTime.now());
        }

        static ErroResponse of(int status, String mensagem, Map<String, String> campos) {
            return new ErroResponse(status, mensagem, campos, LocalDateTime.now());
        }
    }
}