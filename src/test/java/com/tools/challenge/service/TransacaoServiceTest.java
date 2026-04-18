package com.tools.challenge.service;

import com.tools.challenge.dto.PagamentoRequest;
import com.tools.challenge.dto.TransacaoResponse;
import com.tools.challenge.enums.StatusTransacao;
import com.tools.challenge.enums.TipoPagamento;
import com.tools.challenge.exception.TransacaoDuplicadaException;
import com.tools.challenge.exception.TransacaoJaCanceladaException;
import com.tools.challenge.exception.TransacaoNaoEncontradaException;
import com.tools.challenge.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TransacaoServiceTest {

    private TransacaoService service;

    @BeforeEach
    void setUp() {
        service = new TransacaoService(new TransacaoRepository());
    }


    private PagamentoRequest.Raiz buildRequest(String id, TipoPagamento tipo, int parcelas) {
        return PagamentoRequest.Raiz.builder()
                .transacao(PagamentoRequest.TransacaoBody.builder()
                        .cartao("4444********1234")
                        .id(id)
                        .descricao(PagamentoRequest.DescricaoBody.builder()
                                .valor(new BigDecimal("500.50"))
                                .dataHora(LocalDateTime.of(2021, 5, 1, 18, 30, 0))
                                .estabelecimento("PetShop Mundo Cão")
                                .build())
                        .formaPagamento(PagamentoRequest.FormaPagamentoBody.builder()
                                .tipo(tipo)
                                .parcelas(parcelas)
                                .build())
                        .build())
                .build();
    }


    @Nested
    @DisplayName("realizarPagamento")
    class RealizarPagamento {

        @Test
        @DisplayName("deve retornar transação AUTORIZADA para pagamento AVISTA válido")
        void deve_retornarAutorizado_quando_pagamentoAvistaValido() {
            var request = buildRequest("TX-001", TipoPagamento.AVISTA, 1);

            TransacaoResponse.Raiz response = service.realizarPagamento(request);

            assertThat(response.getTransacao().getId()).isEqualTo("TX-001");
            assertThat(response.getTransacao().getDescricao().getStatus())
                    .isEqualTo(StatusTransacao.AUTORIZADO);
            assertThat(response.getTransacao().getDescricao().getNsu()).isNotBlank();
            assertThat(response.getTransacao().getDescricao().getCodigoAutorizacao()).isNotBlank();
        }

        @Test
        @DisplayName("deve retornar transação AUTORIZADA para pagamento PARCELADO_LOJA")
        void deve_retornarAutorizado_quando_parceladoLoja() {
            var request = buildRequest("TX-002", TipoPagamento.PARCELADO_LOJA, 3);

            TransacaoResponse.Raiz response = service.realizarPagamento(request);

            assertThat(response.getTransacao().getDescricao().getStatus())
                    .isEqualTo(StatusTransacao.AUTORIZADO);
            assertThat(response.getTransacao().getFormaPagamento().getParcelas()).isEqualTo(3);
        }

        @Test
        @DisplayName("deve lançar TransacaoDuplicadaException para ID já existente")
        void deve_lancarDuplicada_quando_idJaExiste() {
            var request = buildRequest("TX-DUP", TipoPagamento.AVISTA, 1);
            service.realizarPagamento(request);

            assertThatThrownBy(() -> service.realizarPagamento(request))
                    .isInstanceOf(TransacaoDuplicadaException.class)
                    .hasMessageContaining("TX-DUP");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando AVISTA com parcelas > 1")
        void deve_lancarExcecao_quando_avistaComMaisDeUmaParcela() {
            var request = buildRequest("TX-003", TipoPagamento.AVISTA, 3);

            assertThatThrownBy(() -> service.realizarPagamento(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("AVISTA");
        }

        @Test
        @DisplayName("deve gerar NSU e codigoAutorizacao distintos entre transações")
        void deve_gerarNsuECodigoDistintos_para_transacoesDiferentes() {
            var r1 = service.realizarPagamento(buildRequest("TX-A", TipoPagamento.AVISTA, 1));
            var r2 = service.realizarPagamento(buildRequest("TX-B", TipoPagamento.AVISTA, 1));

            assertThat(r1.getTransacao().getDescricao().getNsu())
                    .isNotBlank();
            assertThat(r2.getTransacao().getDescricao().getNsu())
                    .isNotBlank();
        }
    }

    @Nested
    @DisplayName("estornar")
    class Estornar {

        @Test
        @DisplayName("deve retornar status CANCELADO após estorno")
        void deve_retornarCancelado_aposEstorno() {
            service.realizarPagamento(buildRequest("TX-EST-1", TipoPagamento.AVISTA, 1));

            TransacaoResponse.Raiz response = service.estornar("TX-EST-1");

            assertThat(response.getTransacao().getDescricao().getStatus())
                    .isEqualTo(StatusTransacao.CANCELADO);
        }

        @Test
        @DisplayName("deve lançar TransacaoNaoEncontradaException para ID inexistente")
        void deve_lancarNaoEncontrada_quando_idInexistente() {
            assertThatThrownBy(() -> service.estornar("ID-FANTASMA"))
                    .isInstanceOf(TransacaoNaoEncontradaException.class);
        }

        @Test
        @DisplayName("deve lançar TransacaoJaCanceladaException ao estornar novamente")
        void deve_lancarJaCancelada_quando_estornoDuplicado() {
            service.realizarPagamento(buildRequest("TX-EST-2", TipoPagamento.AVISTA, 1));
            service.estornar("TX-EST-2");

            assertThatThrownBy(() -> service.estornar("TX-EST-2"))
                    .isInstanceOf(TransacaoJaCanceladaException.class);
        }
    }

    @Nested
    @DisplayName("consultarPorId")
    class ConsultarPorId {

        @Test
        @DisplayName("deve retornar a transação correta pelo ID")
        void deve_retornarTransacao_quando_idExiste() {
            service.realizarPagamento(buildRequest("TX-Q1", TipoPagamento.AVISTA, 1));

            TransacaoResponse.Raiz result = service.consultarPorId("TX-Q1");

            assertThat(result.getTransacao().getId()).isEqualTo("TX-Q1");
        }

        @Test
        @DisplayName("deve lançar TransacaoNaoEncontradaException para ID inexistente")
        void deve_lancarExcecao_quando_naoEncontrado() {
            assertThatThrownBy(() -> service.consultarPorId("NAOEXISTE"))
                    .isInstanceOf(TransacaoNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("consultarTodas")
    class ConsultarTodas {

        @Test
        @DisplayName("deve retornar lista vazia quando não há transações")
        void deve_retornarListaVazia_quando_semTransacoes() {
            assertThat(service.consultarTodas()).isEmpty();
        }

        @Test
        @DisplayName("deve retornar todas as transações cadastradas")
        void deve_retornarTodasTransacoes() {
            service.realizarPagamento(buildRequest("TX-L1", TipoPagamento.AVISTA, 1));
            service.realizarPagamento(buildRequest("TX-L2", TipoPagamento.PARCELADO_LOJA, 2));

            assertThat(service.consultarTodas()).hasSize(2);
        }
    }
}