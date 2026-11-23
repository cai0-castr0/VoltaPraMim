package br.ucsal.voltapramim.infraestrutura.eventos;

import br.ucsal.voltapramim.aplicacao.porta.OuvinteDeEvento;
import br.ucsal.voltapramim.dominio.evento.EventoDominio;
import br.ucsal.voltapramim.dominio.evento.ItemCadastrado;
import br.ucsal.voltapramim.dominio.evento.ItemDevolvido;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class EstatisticasObserver implements OuvinteDeEvento {
    private final Map<Class<? extends EventoDominio>, LongAdder> contagens = new ConcurrentHashMap<>();

    @Override public void aoReceber(EventoDominio evento) {
        contagens.computeIfAbsent(evento.getClass(), tipo -> new LongAdder()).increment();
    }

    public long totalDe(Class<? extends EventoDominio> tipo) {
        LongAdder contador = contagens.get(tipo);
        return contador == null ? 0 : contador.sum();
    }

    public long totalDeCadastros() { return totalDe(ItemCadastrado.class); }
    public long totalDeRecebimentos() { return totalDe(ItemRecebido.class); }
    public long totalDeDevolucoes() { return totalDe(ItemDevolvido.class); }
}