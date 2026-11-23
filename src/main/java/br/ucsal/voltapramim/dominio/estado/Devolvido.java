package br.ucsal.voltapramim.dominio.estado;

import br.ucsal.voltapramim.dominio.SituacaoItem;

public final class Devolvido implements EstadoItem {
    @Override public String rotulo() { return "Devolvido"; }
    @Override public SituacaoItem situacao() { return SituacaoItem.DEVOLVIDO; }
    @Override public boolean visivelNaBusca() { return false; }
    @Override public boolean disponivelParaRetirada() { return false; }
    @Override public EstadoItem confirmarRecebimento() { throw EstadoItem.transicaoInvalida("O item já foi devolvido."); }
    @Override public EstadoItem registrarDevolucao() { throw EstadoItem.transicaoInvalida("O item já foi devolvido."); }
}