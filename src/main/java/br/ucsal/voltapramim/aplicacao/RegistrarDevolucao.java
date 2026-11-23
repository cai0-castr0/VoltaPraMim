package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.aplicacao.porta.PublicadorDeEventos;
import br.ucsal.voltapramim.aplicacao.porta.RepositorioDeItens;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.evento.ItemDevolvido;

import java.time.Instant;
import java.util.UUID;

public final class RegistrarDevolucao {
    private final RepositorioDeItens repositorio;
    private final PublicadorDeEventos eventos;

    public RegistrarDevolucao(RepositorioDeItens repositorio, PublicadorDeEventos eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    /**
     * @param posseVerificada confirmação explícita de que o atendente verificou a posse presencialmente
     * @throws PosseNaoVerificadaException se a posse não foi confirmada
     * @throws br.ucsal.voltapramim.dominio.TransicaoInvalidaException se o item não estiver disponível para retirada
     * @throws ItemNaoEncontradoException se o identificador não existir
     */
    public Item executar(UUID id, boolean posseVerificada) {
        if (id == null) throw new IllegalArgumentException("Identificador do item é obrigatório.");
        if (!posseVerificada) throw new PosseNaoVerificadaException();
        Item item = repositorio.atualizar(id, Item::registrarDevolucao).orElseThrow(ItemNaoEncontradoException::new);
        eventos.publicar(new ItemDevolvido(item.getId(), Instant.now()));
        return item;
    }
}