package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import br.ucsal.voltapramim.infraestrutura.persistencia.ItemRepositorioEmMemoria;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmarRecebimentoTest {
    private final ItemRepositorioEmMemoria repositorio = new ItemRepositorioEmMemoria();

    @Test
    void publicaItemRecebidoSomenteDepoisDeAlterarOEstado() {
        AtomicReference<String> estadoNaPublicacao = new AtomicReference<>();
        Item item = novoItem();
        EventosCapturados eventos = new EventosCapturados(evento ->
                estadoNaPublicacao.set(repositorio.buscarPorId(item.getId()).orElseThrow().getEstadoRotulo()));

        Item resultado = new ConfirmarRecebimento(repositorio, eventos).executar(item.getId());

        assertEquals("Disponível para retirada", resultado.getEstadoRotulo());
        assertEquals("Disponível para retirada", estadoNaPublicacao.get());
        assertEquals(1, eventos.publicados.size());
        assertTrue(eventos.publicados.get(0) instanceof ItemRecebido);
        assertEquals(item.getId(), ((ItemRecebido) eventos.publicados.get(0)).itemId());
    }

    @Test
    void repeticaoFalhaSemNovoEventoESemAlterarOItem() {
        Item item = novoItem();
        EventosCapturados eventos = new EventosCapturados();
        ConfirmarRecebimento caso = new ConfirmarRecebimento(repositorio, eventos);
        caso.executar(item.getId());

        assertThrows(TransicaoInvalidaException.class, () -> caso.executar(item.getId()));

        assertEquals(1, eventos.publicados.size());
        assertEquals("Disponível para retirada", item.getEstadoRotulo());
    }

    @Test
    void itemJaDevolvidoNaoPodeSerRecebidoNovamente() {
        Item item = novoItem();
        item.confirmarRecebimento();
        item.registrarDevolucao();
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(TransicaoInvalidaException.class,
                () -> new ConfirmarRecebimento(repositorio, eventos).executar(item.getId()));

        assertTrue(eventos.publicados.isEmpty());
        assertEquals("Devolvido", item.getEstadoRotulo());
    }

    @Test
    void identificadorDesconhecidoNaoPublicaEvento() {
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(ItemNaoEncontradoException.class,
                () -> new ConfirmarRecebimento(repositorio, eventos).executar(UUID.randomUUID()));

        assertTrue(eventos.publicados.isEmpty());
    }

    @Test
    void identificadorNuloEhRejeitado() {
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(IllegalArgumentException.class,
                () -> new ConfirmarRecebimento(repositorio, eventos).executar(null));

        assertTrue(eventos.publicados.isEmpty());
    }

    @Test
    void confirmacoesSimultaneasTemExatamenteUmSucessoEUmEvento() throws Exception {
        Item item = novoItem();
        EventosCapturados eventos = new EventosCapturados();
        ConfirmarRecebimento caso = new ConfirmarRecebimento(repositorio, eventos);
        int tentativas = 16;
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        try {
            CountDownLatch largada = new CountDownLatch(1);
            List<Future<Boolean>> futuros = new ArrayList<>();
            for (int i = 0; i < tentativas; i++) {
                futuros.add(pool.submit(() -> {
                    largada.await();
                    try { caso.executar(item.getId()); return true; }
                    catch (TransicaoInvalidaException erro) { return false; }
                }));
            }
            largada.countDown();
            int sucessos = 0;
            for (Future<Boolean> futuro : futuros) if (futuro.get(10, TimeUnit.SECONDS)) sucessos++;

            assertEquals(1, sucessos);
            assertEquals(1, eventos.publicados.size());
            assertEquals("Disponível para retirada", item.getEstadoRotulo());
        } finally {
            pool.shutdownNow();
        }
    }

    private Item novoItem() {
        Item item = new Item("VPM-ABCDE", "Mochila azul", "", Categoria.BOLSAS, LocalCampus.BIBLIOTECA);
        repositorio.salvar(item);
        return item;
    }
}
