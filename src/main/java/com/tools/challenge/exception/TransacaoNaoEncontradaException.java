package com.tools.challenge.exception;


public class TransacaoNaoEncontradaException extends RuntimeException {

    public TransacaoNaoEncontradaException(String id) {
        super("Transação com id '" + id + "' não encontrada.");
    }
}