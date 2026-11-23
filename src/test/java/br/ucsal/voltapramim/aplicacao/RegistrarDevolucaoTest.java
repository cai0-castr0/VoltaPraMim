package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;
import br.ucsal.voltapramim.dominio.evento.ItemDevolvido;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaPorRelevancia;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaRecentes;
import br.ucsal.voltapramim.infraestrutura.persistencia.ItemRepositorioEmMemoria;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarDevolucaoTest {
    private final ItemRepositorioEmMemoria repositorio = new ItemRepositorioEmMemoria();

    @Test
    void semPosseVerificadaRecusaSemEventoESemAlterarOItem() {
        Item item = novoItemRecebido();
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(PosseNaoVerificadaException.class,
                () -> new RegistrarDevolucao(repositorio, eventos).executar(item.getId(), false));

        assertTrue(eventos.publicados.isEmpty());
        assertEquals("Disponível para retirada", item.getEstadoRotulo());
        assertTrue(item.isVisivelNaBusca());
    }

    @Test
    void devolucaoAntesDoRecebimentoFalhaSemEvento() {
        Item item = novoItem();
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(TransicaoInvalidaException.class,
                () -> new RegistrarDevolucao(repositorio, eventos).executar(item.getId(), true));

        assertTrue(eventos.publicados.isEmpty());
        assertEquals("Aguardando entrega", item.getEstadoRotulo());
    }

    @Test
    void devolucaoValidaPublicaEventoDepoisDeAlterarOEstadoEOcultaDaBusca() {
        Item item = novoItemRecebido();
        AtomicReference<String> estadoNaPublicacao = new AtomicReference<>();
        EventosCapturados eventos = new EventosCapturados(evento ->
                estadoNaPublicacao.set(repositorio.buscarPorId(item.getId()).orElseThrow().getEstadoRotulo()));

        Item resultado = new RegistrarDevolucao(repositorio, eventos).executar(item.getId(), true);

        assertEquals("Devolvido", resultado.getEstadoRotulo());
        assertEquals("Devolvido", estadoNaPublicacao.get());
        assertFalse(resultado.isVisivelNaBusca());
        assertEquals(1, eventos.publicados.size());
        assertTrue(eventos.publicados.get(0) instanceof ItemDevolvido);
        assertEquals(item.getId(), ((ItemDevolvido) eventos.publicados.get(0)).itemId());

        BuscarItens busca = new BuscarItens(repositorio, new BuscaPorRelevancia(), new BuscaRecentes());
        assertTrue(busca.executar("", null, null).isEmpty());
        assertTrue(busca.executar("mochila", null, null).isEmpty());
        assertTrue(repositorio.buscarPorId(item.getId()).isPresent());
    }

    @Test
    void repeticaoDaDevolucaoFalhaSemNovoEvento() {
        Item item = novoItemRecebido();
        EventosCapturados eventos = new EventosCapturados();
        RegistrarDevolucao caso = new RegistrarDevolucao(repositorio, eventos);
        caso.executar(item.getId(), true);

        assertThrows(TransicaoInvalidaException.class, () -> caso.executar(item.getId(), true));

        assertEquals(1, eventos.publicados.size());
        assertEquals("Devolvido", item.getEstadoRotulo());
    }

    @Test
    void identificadorDesconhecidoNaoPublicaEvento() {
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(ItemNaoEncontradoException.class,
                () -> new RegistrarDevolucao(repositorio, eventos).executar(UUID.randomUUID(), true));

        assertTrue(eventos.publicados.isEmpty());
    }

    @Test
    void identificadorNuloEhRejeitado() {
        EventosCapturados eventos = new EventosCapturados();

        assertThrows(IllegalArgumentException.class,
                () -> new RegistrarDevolucao(repositorio, eventos).executar(null, true));

        assertTrue(eventos.publicados.isEmpty());
    }

    @Test
    void fluxoCompletoPublicaRecebidoEDepoisDevolvido() {
        Item item = novoItem();
        EventosCapturados eventos = new EventosCapturados();

        new ConfirmarRecebimento(repositorio, eventos).executar(item.getId());
        new RegistrarDevolucao(repositorio, eventos).executar(item.getId(), true);

        assertEquals(2, eventos.publicados.size());
        assertTrue(eventos.publicados.get(0) instanceof ItemRecebido);
        assertTrue(eventos.publicados.get(1) instanceof ItemDevolvido);
    }

    private Item novoItem() {
        Item item = new Item("VPM-ABCDE", "Mochila azul", "", Categoria.BOLSAS, LocalCampus.BIBLIOTECA);
        repositorio.salvar(item);
        return item;
    }

    private Item novoItemRecebido() {
        Item item = novoItem();
        new ConfirmarRecebimento(repositorio, new EventosCapturados()).executar(item.getId());
        return item;
    }
}
