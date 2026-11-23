package br.ucsal.voltapramim.aplicacao;

import br.ucsal.voltapramim.dominio.Item;
import java.util.List;

public record PainelOperacoes(List<Item> aguardandoEntrega, List<Item> disponiveisParaRetirada, List<Item> devolvidos) {
    public PainelOperacoes {
        aguardandoEntrega = List.copyOf(aguardandoEntrega);
        disponiveisParaRetirada = List.copyOf(disponiveisParaRetirada);
        devolvidos = List.copyOf(devolvidos);
    }

    public List<Item> getAguardandoEntrega() {
        return aguardandoEntrega;
    }

    public List<Item> getDisponiveisParaRetirada() {
        return disponiveisParaRetirada;
    }

    public List<Item> getDevolvidos() {
        return devolvidos;
    }
}