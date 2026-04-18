package com.tools.challenge.service;

import com.tools.challenge.dto.PagamentoRequest;
import com.tools.challenge.dto.TransacaoResponse;
import com.tools.challenge.enums.StatusTransacao;
import com.tools.challenge.enums.TipoPagamento;
import com.tools.challenge.exception.TransacaoDuplicadaException;
import com.tools.challenge.exception.TransacaoJaCanceladaException;
import com.tools.challenge.exception.TransacaoNaoEncontradaException;
import com.tools.challenge.model.Descricao;
import com.tools.challenge.model.FormaPagamento;
import com.tools.challenge.model.Transacao;
import com.tools.challenge.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public TransacaoResponse.Raiz realizarPagamento(PagamentoRequest.Raiz request) {
        PagamentoRequest.TransacaoBody body = request.getTransacao();

        if (repository.existePorId(body.getId())) {
            throw new TransacaoDuplicadaException(body.getId());
        }

        if (body.getFormaPagamento().getTipo() == TipoPagamento.AVISTA
                && body.getFormaPagamento().getParcelas() != 1) {
            throw new IllegalArgumentException("Pagamento AVISTA deve ter exatamente 1 parcela.");
        }

        Transacao transacao = Transacao.builder()
                .id(body.getId())
                .cartao(body.getCartao())
                .descricao(Descricao.builder()
                        .valor(body.getDescricao().getValor())
                        .dataHora(body.getDescricao().getDataHora())
                        .estabelecimento(body.getDescricao().getEstabelecimento())
                        .nsu(gerarNsu())
                        .codigoAutorizacao(gerarCodigoAutorizacao())
                        .status(StatusTransacao.AUTORIZADO)
                        .build())
                .formaPagamento(FormaPagamento.builder()
                        .tipo(body.getFormaPagamento().getTipo())
                        .parcelas(body.getFormaPagamento().getParcelas())
                        .build())
                .build();

        repository.salvar(transacao);
        return toResponse(transacao);
    }

    public TransacaoResponse.Raiz estornar(String id) {
        Transacao transacao = buscarOuLancar(id);

        if (transacao.getDescricao().getStatus() == StatusTransacao.CANCELADO) {
            throw new TransacaoJaCanceladaException(id);
        }

        transacao.getDescricao().setStatus(StatusTransacao.CANCELADO);
        repository.salvar(transacao);
        return toResponse(transacao);
    }

    public TransacaoResponse.Raiz consultarPorId(String id) {
        return toResponse(buscarOuLancar(id));
    }

    public List<TransacaoResponse.Raiz> consultarTodas() {
        return repository.buscarTodas().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Transacao buscarOuLancar(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new TransacaoNaoEncontradaException(id));
    }

    private TransacaoResponse.Raiz toResponse(Transacao t) {
        return TransacaoResponse.Raiz.builder()
                .transacao(TransacaoResponse.TransacaoBody.builder()
                        .cartao(t.getCartao())
                        .id(t.getId())
                        .descricao(TransacaoResponse.DescricaoBody.builder()
                                .valor(t.getDescricao().getValor())
                                .dataHora(t.getDescricao().getDataHora())
                                .estabelecimento(t.getDescricao().getEstabelecimento())
                                .nsu(t.getDescricao().getNsu())
                                .codigoAutorizacao(t.getDescricao().getCodigoAutorizacao())
                                .status(t.getDescricao().getStatus())
                                .build())
                        .formaPagamento(TransacaoResponse.FormaPagamentoBody.builder()
                                .tipo(t.getFormaPagamento().getTipo())
                                .parcelas(t.getFormaPagamento().getParcelas())
                                .build())
                        .build())
                .build();
    }

    private String gerarNsu() {
        return String.valueOf((long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }

    private String gerarCodigoAutorizacao() {
        String digits = (UUID.randomUUID() + UUID.randomUUID().toString())
                .replaceAll("[^0-9]", "");
        return digits.substring(0, 9);
    }
}