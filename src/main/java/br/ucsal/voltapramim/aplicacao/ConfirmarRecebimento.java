package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.aplicacao.porta.PublicadorDeEventos;
import br.ucsal.voltapramim.aplicacao.porta.RepositorioDeItens;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;

import java.time.Instant;
import java.util.UUID;

public final class ConfirmarRecebimento {
    private final RepositorioDeItens repositorio;
    private final PublicadorDeEventos eventos;

    public ConfirmarRecebimento(RepositorioDeItens repositorio, PublicadorDeEventos eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    /**
     * @throws br.ucsal.voltapramim.dominio.TransicaoInvalidaException se o item não estiver aguardando entrega
     * @throws ItemNaoEncontradoException se o identificador não existir
     */
    public Item executar(UUID id) {
        if (id == null) throw new IllegalArgumentException("Identificador do item é obrigatório.");
        Item item = repositorio.atualizar(id, Item::confirmarRecebimento).orElseThrow(ItemNaoEncontradoException::new);
        eventos.publicar(new ItemRecebido(item.getId(), Instant.now()));
        return item;
    }
}