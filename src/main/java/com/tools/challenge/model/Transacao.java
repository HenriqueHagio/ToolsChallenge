package com.tools.challenge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transacao {

    private String id;

    private String cartao;
    private Descricao descricao;
    private FormaPagamento formaPagamento;
}