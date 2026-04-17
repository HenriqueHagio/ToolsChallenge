package com.tools.challenge.model;

import com.tools.challenge.enums.TipoPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa a forma de pagamento de uma transação.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagamento {

    private TipoPagamento tipo;


    private Integer parcelas;
}