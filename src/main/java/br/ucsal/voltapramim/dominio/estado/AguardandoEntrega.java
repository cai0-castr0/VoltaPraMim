package br.ucsal.voltapramim.dominio.estado;

import br.ucsal.voltapramim.dominio.SituacaoItem;

public final class AguardandoEntrega implements EstadoItem {
    @Override public String rotulo() { return "Aguardando entrega"; }
    @Override public SituacaoItem situacao() { return SituacaoItem.AGUARDANDO_ENTREGA; }
    @Override public boolean visivelNaBusca() { return true; }
    @Override public boolean disponivelParaRetirada() { return false; }
    @Override public EstadoItem confirmarRecebimento() { return new DisponivelRetirada(); }
    @Override public EstadoItem registrarDevolucao() { throw EstadoItem.transicaoInvalida("O item precisa ser recebido antes da devolução."); }
}