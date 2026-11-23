package br.ucsal.voltapramim.dominio.estado;

import br.ucsal.voltapramim.dominio.SituacaoItem;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;

public interface EstadoItem {
    String rotulo();
    SituacaoItem situacao();
    boolean visivelNaBusca();
    boolean disponivelParaRetirada();
    EstadoItem confirmarRecebimento();
    EstadoItem registrarDevolucao();

    static TransicaoInvalidaException transicaoInvalida(String mensagem) {
        return new TransicaoInvalidaException(mensagem);
    }
}