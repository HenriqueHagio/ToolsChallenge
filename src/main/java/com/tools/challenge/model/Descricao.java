package com.tools.challenge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detalhes descritivos de uma transação.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Descricao {

    private BigDecimal valor;
    private LocalDateTime dataHora;
    private String estabelecimento;

    private String nsu;

    private String codigoAutorizacao;

    private com.tools.challenge.enums.StatusTransacao status;
}