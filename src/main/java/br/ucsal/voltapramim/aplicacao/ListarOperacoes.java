package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.aplicacao.porta.RepositorioDeItens;
import br.ucsal.voltapramim.dominio.Item;
import br.ucsal.voltapramim.dominio.SituacaoItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ListarOperacoes {
    private final RepositorioDeItens repositorio;

    public ListarOperacoes(RepositorioDeItens repositorio) { this.repositorio = repositorio; }

    public PainelOperacoes executar() {
        Map<SituacaoItem, List<Item>> porSituacao = repositorio.listarTodos().stream()
                .collect(Collectors.groupingBy(Item::getSituacao));
        return new PainelOperacoes(
                porSituacao.getOrDefault(SituacaoItem.AGUARDANDO_ENTREGA, List.of()),
                porSituacao.getOrDefault(SituacaoItem.DISPONIVEL_RETIRADA, List.of()),
                porSituacao.getOrDefault(SituacaoItem.DEVOLVIDO, List.of()));
    }
}