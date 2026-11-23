package br.ucsal.voltapramim.aplicacao;

public final class ItemNaoEncontradoException extends RuntimeException {
    public ItemNaoEncontradoException() { super("Item não encontrado."); }
}