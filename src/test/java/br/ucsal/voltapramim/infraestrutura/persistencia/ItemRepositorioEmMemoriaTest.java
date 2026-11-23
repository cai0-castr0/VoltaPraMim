package br.ucsal.voltapramim.infraestrutura.persistencia;

import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRepositorioEmMemoriaTest {
    private final ItemRepositorioEmMemoria repositorio = new ItemRepositorioEmMemoria();

    @Test
    void atualizarAplicaAOperacaoEDevolveOItem() {
        Item item = novoItem("VPM-AAAAA");

        Item resultado = repositorio.atualizar(item.getId(), Item::confirmarRecebimento).orElseThrow();

        assertSame(item, resultado);
        assertEquals("Disponível para retirada", item.getEstadoRotulo());
    }

    @Test
    void atualizarComIdInexistenteOuNuloNaoExecutaAOperacao() {
        AtomicInteger chamadas = new AtomicInteger();

        assertTrue(repositorio.atualizar(UUID.randomUUID(), item -> chamadas.incrementAndGet()).isEmpty());
        assertTrue(repositorio.atualizar(null, item -> chamadas.incrementAndGet()).isEmpty());

        assertEquals(0, chamadas.get());
    }

    @Test
    void operacaoQueFalhaPropagaAExcecaoEMantemOEstado() {
        Item item = novoItem("VPM-AAAAA");

        assertThrows(TransicaoInvalidaException.class,
                () -> repositorio.atualizar(item.getId(), Item::registrarDevolucao));

        assertEquals("Aguardando entrega", item.getEstadoRotulo());
        assertTrue(repositorio.buscarPorId(item.getId()).isPresent());
    }

    @Test
    void listarTodosInclueDevolvidosMasListarAtivosNao() {
        Item ativo = novoItem("VPM-AAAAA");
        Item devolvido = novoItem("VPM-BBBBB");
        devolvido.confirmarRecebimento();
        devolvido.registrarDevolucao();

        assertEquals(2, repositorio.listarTodos().size());
        assertEquals(1, repositorio.listarAtivos().size());
        assertEquals(ativo.getId(), repositorio.listarAtivos().get(0).getId());
        assertFalse(repositorio.listarAtivos().contains(devolvido));
    }

    @Test
    void atualizacoesSimultaneasDoMesmoItemSaoSerializadas() throws Exception {
        Item item = novoItem("VPM-AAAAA");
        int threads = 8;
        int repeticoes = 1000;
        int[] contador = new int[1];
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch largada = new CountDownLatch(1);
            List<Future<Void>> futuros = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futuros.add(pool.submit(() -> {
                    largada.await();
                    for (int j = 0; j < repeticoes; j++) {
                        repositorio.atualizar(item.getId(), alvo -> contador[0]++);
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<Void> futuro : futuros) futuro.get(20, TimeUnit.SECONDS);

            assertEquals(threads * repeticoes, contador[0]);
        } finally {
            pool.shutdownNow();
        }
    }

    private Item novoItem(String referencia) {
        Item item = new Item(referencia, "Mochila azul", "", Categoria.BOLSAS, LocalCampus.BIBLIOTECA);
        repositorio.salvar(item);
        return item;
    }
}
