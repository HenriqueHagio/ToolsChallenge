package com.tools.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tools.challenge.enums.StatusTransacao;
import com.tools.challenge.enums.TipoPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class TransacaoResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Raiz {
        private TransacaoBody transacao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransacaoBody {
        private String cartao;
        private String id;
        private DescricaoBody descricao;
        private FormaPagamentoBody formaPagamento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DescricaoBody {

        private BigDecimal valor;

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        private LocalDateTime dataHora;

        private String estabelecimento;
        private String nsu;
        private String codigoAutorizacao;
        private StatusTransacao status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormaPagamentoBody {
        private TipoPagamento tipo;
        private Integer parcelas;
    }
}