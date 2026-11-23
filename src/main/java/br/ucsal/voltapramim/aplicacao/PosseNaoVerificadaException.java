package br.ucsal.voltapramim.aplicacao;

public final class PosseNaoVerificadaException extends IllegalArgumentException {
    public PosseNaoVerificadaException() {
        super("A posse precisa ser verificada presencialmente pelo atendente antes da devolução.");
    }
}