package com.tools.challenge.exception;


public class TransacaoDuplicadaException extends RuntimeException {

    public TransacaoDuplicadaException(String id) {
        super("Transação com id '" + id + "' já existe.");
    }
}