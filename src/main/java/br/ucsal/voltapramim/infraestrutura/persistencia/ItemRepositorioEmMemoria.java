package br.ucsal.voltapramim.infraestrutura.persistencia;

import br.ucsal.voltapramim.aplicacao.porta.RepositorioDeItens;
import br.ucsal.voltapramim.dominio.Item;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ItemRepositorioEmMemoria implements RepositorioDeItens {
    private final ConcurrentHashMap<UUID, Item> itens = new ConcurrentHashMap<>();

    @Override public void salvar(Item item) { itens.put(item.getId(), item); }
    @Override public Optional<Item> buscarPorId(UUID id) { return Optional.ofNullable(itens.get(id)); }
    @Override public List<Item> listarAtivos() {
        return itens.values().stream().filter(Item::isVisivelNaBusca)
                .sorted(Comparator.comparing(Item::getEncontradoEm).reversed()).toList();
    }
    @Override public List<Item> listarTodos() {
        return itens.values().stream().sorted(Comparator.comparing(Item::getEncontradoEm).reversed()).toList();
    }
    @Override public boolean existeReferencia(String referencia) {
        return itens.values().stream().anyMatch(item -> item.getReferenciaPublica().equals(referencia));
    }

    /** A operação roda dentro de computeIfPresent: o bloqueio por chave impede atualizações simultâneas do mesmo item. */
    @Override public Optional<Item> atualizar(UUID id, Consumer<Item> operacao) {
        Objects.requireNonNull(operacao);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(itens.computeIfPresent(id, (chave, item) -> {
            operacao.accept(item);
            return item;
        }));
    }
}