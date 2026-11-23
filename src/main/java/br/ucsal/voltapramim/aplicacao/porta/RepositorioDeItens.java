package br.ucsal.voltapramim.aplicacao.porta;

import br.ucsal.voltapramim.dominio.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface RepositorioDeItens {
    void salvar(Item item);
    Optional<Item> buscarPorId(UUID id);
    List<Item> listarAtivos();
    /** Todos os itens, inclusive devolvidos, do mais recente para o mais antigo. Uso interno do painel. */
    List<Item> listarTodos();
    boolean existeReferencia(String referencia);

    /**
     * Aplica a operação ao item de forma atômica: nenhuma outra atualização do mesmo item ocorre durante a operação.
     * Se a operação lançar exceção, ela é propagada e o item não é considerado alterado.
     *
     * @return o item atualizado, ou vazio se o identificador não existir
     */
    Optional<Item> atualizar(UUID id, Consumer<Item> operacao);
}