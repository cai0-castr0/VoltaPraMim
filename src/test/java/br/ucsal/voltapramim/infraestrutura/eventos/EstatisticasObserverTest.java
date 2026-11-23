package br.ucsal.voltapramim.infraestrutura.eventos;

import br.ucsal.voltapramim.dominio.evento.ItemCadastrado;
import br.ucsal.voltapramim.dominio.evento.ItemDevolvido;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstatisticasObserverTest {
    @Test
    void contaCadaTipoDeEventoSeparadamente() {
        EstatisticasObserver estatisticas = new EstatisticasObserver();
        UUID id = UUID.randomUUID();

        estatisticas.aoReceber(new ItemCadastrado(id, Instant.now()));
        estatisticas.aoReceber(new ItemCadastrado(id, Instant.now()));
        estatisticas.aoReceber(new ItemRecebido(id, Instant.now()));
        estatisticas.aoReceber(new ItemDevolvido(id, Instant.now()));

        assertEquals(2, estatisticas.totalDeCadastros());
        assertEquals(1, estatisticas.totalDeRecebimentos());
        assertEquals(1, estatisticas.totalDeDevolucoes());
        assertEquals(2, estatisticas.totalDe(ItemCadastrado.class));
    }

    @Test
    void eventosAdministrativosNaoInflamOsCadastros() {
        EstatisticasObserver estatisticas = new EstatisticasObserver();

        estatisticas.aoReceber(new ItemRecebido(UUID.randomUUID(), Instant.now()));
        estatisticas.aoReceber(new ItemDevolvido(UUID.randomUUID(), Instant.now()));

        assertEquals(0, estatisticas.totalDeCadastros());
    }

    @Test
    void tipoSemEventosRetornaZero() {
        assertEquals(0, new EstatisticasObserver().totalDe(ItemDevolvido.class));
    }
}
