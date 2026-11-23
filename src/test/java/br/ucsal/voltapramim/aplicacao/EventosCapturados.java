package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.aplicacao.porta.OuvinteDeEvento;
import br.ucsal.voltapramim.aplicacao.porta.PublicadorDeEventos;
import br.ucsal.voltapramim.dominio.evento.EventoDominio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Publicador de teste: guarda os eventos e permite inspecionar o mundo no instante da publicação. */
final class EventosCapturados implements PublicadorDeEventos {
    final List<EventoDominio> publicados = new CopyOnWriteArrayList<>();
    private final Consumer<EventoDominio> aoPublicar;

    EventosCapturados() { this(evento -> { }); }

    EventosCapturados(Consumer<EventoDominio> aoPublicar) { this.aoPublicar = aoPublicar; }

    @Override public void inscrever(OuvinteDeEvento ouvinte) { }

    @Override public void publicar(EventoDominio evento) {
        aoPublicar.accept(evento);
        publicados.add(evento);
    }
}
