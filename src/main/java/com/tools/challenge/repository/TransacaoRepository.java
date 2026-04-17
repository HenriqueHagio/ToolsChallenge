package com.tools.challenge.repository;

import com.tools.challenge.model.Transacao;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class TransacaoRepository {

    private final Map<String, Transacao> store = new ConcurrentHashMap<>();

    public Transacao salvar(Transacao transacao) {
        store.put(transacao.getId(), transacao);
        return transacao;
    }

    public Optional<Transacao> buscarPorId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Transacao> buscarTodas() {
        return new ArrayList<>(store.values());
    }

    public boolean existePorId(String id) {
        return store.containsKey(id);
    }
}