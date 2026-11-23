package br.ucsal.voltapramim.infraestrutura.eventos;

import br.ucsal.voltapramim.dominio.evento.EventoDominio;
import br.ucsal.voltapramim.dominio.evento.ItemCadastrado;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicadorEmMemoriaTest {
    @Test
    void notificaDoisObservadoresComOMesmoEvento() {
        PublicadorEmMemoria publicador = new PublicadorEmMemoria();
        List<EventoDominio> primeiro = new ArrayList<>();
        List<EventoDominio> segundo = new ArrayList<>();
        publicador.inscrever(primeiro::add);
        publicador.inscrever(segundo::add);
        EventoDominio evento = new ItemCadastrado(UUID.randomUUID(), Instant.now());

        publicador.publicar(evento);

        assertEquals(List.of(evento), primeiro);
        assertEquals(List.of(evento), segundo);
    }

    @Test
    void falhaEmUmObservadorNaoImpedeANotificacaoDoSeguinte() {
        PublicadorEmMemoria publicador = new PublicadorEmMemoria();
        List<EventoDominio> recebidos = new ArrayList<>();
        publicador.inscrever(evento -> { throw new IllegalStateException("falha simulada"); });
        publicador.inscrever(recebidos::add);
        EventoDominio evento = new ItemCadastrado(UUID.randomUUID(), Instant.now());

        publicador.publicar(evento);

        assertEquals(List.of(evento), recebidos);
    }

    @Test
    void publicarSemObservadoresNaoFalha() {
        new PublicadorEmMemoria().publicar(new ItemCadastrado(UUID.randomUUID(), Instant.now()));
    }

    @Test
    void rejeitaObservadorNulo() {
        assertThrows(NullPointerException.class, () -> new PublicadorEmMemoria().inscrever(null));
    }
}
