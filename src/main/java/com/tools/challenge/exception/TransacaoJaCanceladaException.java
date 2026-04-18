package com.tools.challenge.exception;


public class TransacaoJaCanceladaException extends RuntimeException {

    public TransacaoJaCanceladaException(String id) {
        super("Transação com id '" + id + "' já foi cancelada e não pode ser estornada novamente.");
    }
}