package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.infraestrutura.persistencia.ItemRepositorioEmMemoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListarOperacoesTest {
    @Test
    void separaItensPorEstadoIncluindoHistoricoDeDevolvidos() {
        ItemRepositorioEmMemoria repositorio = new ItemRepositorioEmMemoria();
        Item aguardando = novoItem(repositorio, "VPM-AAAAA", "Chave");
        Item disponivel = novoItem(repositorio, "VPM-BBBBB", "Estojo");
        Item devolvido = novoItem(repositorio, "VPM-CCCCC", "Mochila");
        disponivel.confirmarRecebimento();
        devolvido.confirmarRecebimento();
        devolvido.registrarDevolucao();

        PainelOperacoes painel = new ListarOperacoes(repositorio).executar();

        assertEquals(1, painel.aguardandoEntrega().size());
        assertEquals(aguardando.getId(), painel.aguardandoEntrega().get(0).getId());
        assertEquals(1, painel.disponiveisParaRetirada().size());
        assertEquals(disponivel.getId(), painel.disponiveisParaRetirada().get(0).getId());
        assertEquals(1, painel.devolvidos().size());
        assertEquals(devolvido.getId(), painel.devolvidos().get(0).getId());
    }

    @Test
    void repositorioVazioGeraListasVazias() {
        PainelOperacoes painel = new ListarOperacoes(new ItemRepositorioEmMemoria()).executar();

        assertTrue(painel.aguardandoEntrega().isEmpty());
        assertTrue(painel.disponiveisParaRetirada().isEmpty());
        assertTrue(painel.devolvidos().isEmpty());
    }

    private Item novoItem(ItemRepositorioEmMemoria repositorio, String referencia, String titulo) {
        Item item = new Item(referencia, titulo, "", Categoria.OUTROS, LocalCampus.CANTINA);
        repositorio.salvar(item);
        return item;
    }
}
