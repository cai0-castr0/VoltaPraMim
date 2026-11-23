package br.ucsal.voltapramim.dominio.estado;

import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.dominio.SituacaoItem;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoItemTest {
    @Test
    void aguardandoEntregaReceberAvancaEDevolverFalha() {
        EstadoItem estado = new AguardandoEntrega();

        assertEquals(SituacaoItem.AGUARDANDO_ENTREGA, estado.situacao());
        assertTrue(estado.visivelNaBusca());
        assertFalse(estado.disponivelParaRetirada());
        assertTrue(estado.confirmarRecebimento() instanceof DisponivelRetirada);
        assertThrows(TransicaoInvalidaException.class, estado::registrarDevolucao);
    }

    @Test
    void disponivelRetiradaDevolverAvancaEReceberDeNovoFalha() {
        EstadoItem estado = new DisponivelRetirada();

        assertEquals(SituacaoItem.DISPONIVEL_RETIRADA, estado.situacao());
        assertTrue(estado.visivelNaBusca());
        assertTrue(estado.disponivelParaRetirada());
        assertTrue(estado.registrarDevolucao() instanceof Devolvido);
        assertThrows(TransicaoInvalidaException.class, estado::confirmarRecebimento);
    }

    @Test
    void devolvidoRejeitaQualquerTransicaoEFicaForaDaBusca() {
        EstadoItem estado = new Devolvido();

        assertEquals(SituacaoItem.DEVOLVIDO, estado.situacao());
        assertFalse(estado.visivelNaBusca());
        assertFalse(estado.disponivelParaRetirada());
        assertThrows(TransicaoInvalidaException.class, estado::confirmarRecebimento);
        assertThrows(TransicaoInvalidaException.class, estado::registrarDevolucao);
    }

    @Test
    void itemRejeitaSaltoERepeticaoSemMudarDeEstado() {
        Item item = novoItem();
        assertThrows(TransicaoInvalidaException.class, item::registrarDevolucao);
        assertEquals(SituacaoItem.AGUARDANDO_ENTREGA, item.getSituacao());

        item.confirmarRecebimento();
        assertThrows(TransicaoInvalidaException.class, item::confirmarRecebimento);
        assertEquals(SituacaoItem.DISPONIVEL_RETIRADA, item.getSituacao());
        assertTrue(item.isDisponivelParaRetirada());

        item.registrarDevolucao();
        assertThrows(TransicaoInvalidaException.class, item::registrarDevolucao);
        assertThrows(TransicaoInvalidaException.class, item::confirmarRecebimento);
        assertEquals(SituacaoItem.DEVOLVIDO, item.getSituacao());
        assertFalse(item.isDisponivelParaRetirada());
        assertFalse(item.isVisivelNaBusca());
    }

    @Test
    void itemNovoNaoEstaDisponivelParaRetirada() {
        Item item = novoItem();

        assertFalse(item.isDisponivelParaRetirada());
        assertTrue(item.isVisivelNaBusca());
    }

    private Item novoItem() {
        return new Item("VPM-ABCDE", "Mochila azul", "", Categoria.BOLSAS, LocalCampus.BIBLIOTECA);
    }
}