package com.tools.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tools.challenge.enums.TipoPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class PagamentoRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Raiz {

        @Valid
        @NotNull(message = "transacao é obrigatória")
        private TransacaoBody transacao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransacaoBody {

        @NotBlank(message = "cartao é obrigatório")
        private String cartao;

        @NotBlank(message = "id é obrigatório")
        private String id;

        @Valid
        @NotNull(message = "descricao é obrigatória")
        private DescricaoBody descricao;

        @Valid
        @NotNull(message = "formaPagamento é obrigatória")
        private FormaPagamentoBody formaPagamento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DescricaoBody {

        @NotNull(message = "valor é obrigatório")
        @Positive(message = "valor deve ser positivo")
        private BigDecimal valor;

        @NotNull(message = "dataHora é obrigatória")
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        private LocalDateTime dataHora;

        @NotBlank(message = "estabelecimento é obrigatório")
        private String estabelecimento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormaPagamentoBody {

        @NotNull(message = "tipo é obrigatório")
        private TipoPagamento tipo;

        @NotNull(message = "parcelas é obrigatório")
        @Min(value = 1, message = "parcelas deve ser no mínimo 1")
        private Integer parcelas;
    }
}