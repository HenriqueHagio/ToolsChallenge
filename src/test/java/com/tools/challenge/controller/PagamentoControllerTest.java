package com.tools.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.challenge.dto.PagamentoRequest;
import com.tools.challenge.enums.TipoPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PagamentoControllerTest {

    private static final String BASE_URL = "/api/pagamento";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Nested
    @DisplayName("POST /api/pagamento")
    class PostPagamento {

        @Test
        @DisplayName("deve retornar 201 e transação AUTORIZADA para pagamento válido")
        void deve_retornar201_quando_pagamentoValido() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildRequest("TX-C1", TipoPagamento.AVISTA, 1))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transacao.id").value("TX-C1"))
                    .andExpect(jsonPath("$.transacao.descricao.status").value("AUTORIZADO"))
                    .andExpect(jsonPath("$.transacao.descricao.nsu").isNotEmpty())
                    .andExpect(jsonPath("$.transacao.descricao.codigoAutorizacao").isNotEmpty())
                    .andExpect(jsonPath("$.transacao.formaPagamento.tipo").value("AVISTA"))
                    .andExpect(jsonPath("$.transacao.formaPagamento.parcelas").value(1));
        }

        @Test
        @DisplayName("deve retornar 201 para pagamento PARCELADO_LOJA com 3 parcelas")
        void deve_retornar201_quando_parceladoLojaValido() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildRequest("TX-C2", TipoPagamento.PARCELADO_LOJA, 3))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transacao.formaPagamento.tipo").value("PARCELADO_LOJA"))
                    .andExpect(jsonPath("$.transacao.formaPagamento.parcelas").value(3));
        }

        @Test
        @DisplayName("deve retornar 422 quando ID já existe (duplicado)")
        void deve_retornar422_quando_idDuplicado() throws Exception {
            var request = toJson(buildRequest("TX-DUP", TipoPagamento.AVISTA, 1));

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.mensagem").value(containsString("TX-DUP")));
        }

        @Test
        @DisplayName("deve retornar 422 quando AVISTA com mais de 1 parcela")
        void deve_retornar422_quando_avistaComMaisDeUmaParcela() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildRequest("TX-C3", TipoPagamento.AVISTA, 3))))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("deve retornar 400 quando corpo da requisição é inválido (sem cartao)")
        void deve_retornar400_quando_campoObrigatorioAusente() throws Exception {
            // Cria request sem cartao
            var semCartao = PagamentoRequest.Raiz.builder()
                    .transacao(PagamentoRequest.TransacaoBody.builder()
                            .cartao("") // vazio — viola @NotBlank
                            .id("TX-C4")
                            .descricao(PagamentoRequest.DescricaoBody.builder()
                                    .valor(new BigDecimal("100.00"))
                                    .dataHora(LocalDateTime.now())
                                    .estabelecimento("Loja X")
                                    .build())
                            .formaPagamento(PagamentoRequest.FormaPagamentoBody.builder()
                                    .tipo(TipoPagamento.AVISTA)
                                    .parcelas(1)
                                    .build())
                            .build())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(semCartao)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/pagamento/{id}/estorno")
    class PostEstorno {

        @Test
        @DisplayName("deve retornar 200 e status CANCELADO para estorno válido")
        void deve_retornar200_quando_estornoValido() throws Exception {
            // Arrange: cria a transação primeiro
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(buildRequest("TX-E1", TipoPagamento.AVISTA, 1))));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/TX-E1/estorno"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transacao.descricao.status").value("CANCELADO"));
        }

        @Test
        @DisplayName("deve retornar 404 para estorno de ID inexistente")
        void deve_retornar404_quando_idNaoExiste() throws Exception {
            mockMvc.perform(post(BASE_URL + "/ID-FANTASMA/estorno"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensagem").value(containsString("ID-FANTASMA")));
        }

        @Test
        @DisplayName("deve retornar 422 ao tentar estornar transação já cancelada")
        void deve_retornar422_quando_transacaoJaCancelada() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(buildRequest("TX-E2", TipoPagamento.AVISTA, 1))));

            mockMvc.perform(post(BASE_URL + "/TX-E2/estorno"));

            mockMvc.perform(post(BASE_URL + "/TX-E2/estorno"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.mensagem").value(containsString("TX-E2")));
        }
    }

    @Nested
    @DisplayName("GET /api/pagamento")
    class GetTodas {

        @Test
        @DisplayName("deve retornar lista vazia quando não há transações")
        void deve_retornarListaVazia() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("deve retornar todas as transações cadastradas")
        void deve_retornarTodasTransacoes() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(buildRequest("TX-G1", TipoPagamento.AVISTA, 1))));
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(buildRequest("TX-G2", TipoPagamento.PARCELADO_EMISSOR, 6))));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }


    @Nested
    @DisplayName("GET /api/pagamento/{id}")
    class GetPorId {

        @Test
        @DisplayName("deve retornar 200 e a transação correta pelo ID")
        void deve_retornar200_quando_idExiste() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(buildRequest("TX-ID1", TipoPagamento.AVISTA, 1))));

            mockMvc.perform(get(BASE_URL + "/TX-ID1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transacao.id").value("TX-ID1"))
                    .andExpect(jsonPath("$.transacao.cartao").value("4444********1234"));
        }

        @Test
        @DisplayName("deve retornar 404 para ID inexistente")
        void deve_retornar404_quando_idNaoExiste() throws Exception {
            mockMvc.perform(get(BASE_URL + "/NAOEXISTE"))
                    .andExpect(status().isNotFound());
        }
    }
}