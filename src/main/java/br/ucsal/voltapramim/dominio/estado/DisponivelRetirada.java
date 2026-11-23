package br.ucsal.voltapramim.dominio.estado;

import br.ucsal.voltapramim.dominio.SituacaoItem;

public final class DisponivelRetirada implements EstadoItem {
    @Override public String rotulo() { return "Disponível para retirada"; }
    @Override public SituacaoItem situacao() { return SituacaoItem.DISPONIVEL_RETIRADA; }
    @Override public boolean visivelNaBusca() { return true; }
    @Override public boolean disponivelParaRetirada() { return true; }
    @Override public EstadoItem confirmarRecebimento() { throw EstadoItem.transicaoInvalida("O item já foi recebido."); }
    @Override public EstadoItem registrarDevolucao() { return new Devolvido(); }
}