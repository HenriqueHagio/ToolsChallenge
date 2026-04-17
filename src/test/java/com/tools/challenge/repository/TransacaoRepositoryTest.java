package com.tools.challenge.repository;

import com.tools.challenge.enums.StatusTransacao;
import com.tools.challenge.enums.TipoPagamento;
import com.tools.challenge.model.Descricao;
import com.tools.challenge.model.FormaPagamento;
import com.tools.challenge.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class TransacaoRepositoryTest {

    private TransacaoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransacaoRepository();
    }

    private Transacao buildTransacao(String id) {
        return Transacao.builder()
                .id(id)
                .cartao("4444********1234")
                .descricao(Descricao.builder()
                        .valor(new BigDecimal("100.00"))
                        .dataHora(LocalDateTime.now())
                        .estabelecimento("Teste")
                        .nsu("1234567890")
                        .codigoAutorizacao("123456789")
                        .status(StatusTransacao.AUTORIZADO)
                        .build())
                .formaPagamento(FormaPagamento.builder()
                        .tipo(TipoPagamento.AVISTA)
                        .parcelas(1)
                        .build())
                .build();
    }

    @Test
    @DisplayName("salvar deve persistir e retornar a transação")
    void deve_salvarERetornarTransacao() {
        Transacao t = buildTransacao("ID-1");
        Transacao salva = repository.salvar(t);
        assertThat(salva.getId()).isEqualTo("ID-1");
    }

    @Test
    @DisplayName("buscarPorId deve retornar Optional preenchido para ID existente")
    void deve_retornarOptionalPreenchido_quando_idExiste() {
        repository.salvar(buildTransacao("ID-2"));
        Optional<Transacao> result = repository.buscarPorId("ID-2");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("ID-2");
    }

    @Test
    @DisplayName("buscarPorId deve retornar Optional vazio para ID inexistente")
    void deve_retornarOptionalVazio_quando_idNaoExiste() {
        assertThat(repository.buscarPorId("NAOEXISTE")).isEmpty();
    }

    @Test
    @DisplayName("existePorId deve retornar true quando ID cadastrado")
    void deve_retornarTrue_quando_idExiste() {
        repository.salvar(buildTransacao("ID-3"));
        assertThat(repository.existePorId("ID-3")).isTrue();
    }

    @Test
    @DisplayName("existePorId deve retornar false quando ID não cadastrado")
    void deve_retornarFalse_quando_idNaoExiste() {
        assertThat(repository.existePorId("NAOEXISTE")).isFalse();
    }

    @Test
    @DisplayName("buscarTodas deve retornar todas as transações cadastradas")
    void deve_retornarTodas() {
        repository.salvar(buildTransacao("ID-4"));
        repository.salvar(buildTransacao("ID-5"));
        assertThat(repository.buscarTodas()).hasSize(2);
    }

    @Test
    @DisplayName("salvar com mesmo ID deve sobrescrever (upsert)")
    void deve_sobrescrever_quando_mesmId() {
        Transacao original = buildTransacao("ID-6");
        repository.salvar(original);

        Transacao atualizada = buildTransacao("ID-6");
        atualizada.getDescricao().setStatus(StatusTransacao.CANCELADO);
        repository.salvar(atualizada);

        assertThat(repository.buscarPorId("ID-6").get().getDescricao().getStatus())
                .isEqualTo(StatusTransacao.CANCELADO);
        assertThat(repository.buscarTodas()).hasSize(1);
    }
}