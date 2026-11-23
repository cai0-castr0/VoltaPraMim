package br.ucsal.voltapramim.web;

import java.util.Map;
import java.util.Optional;

public final class Roteador {
    private final Map<ChaveRota, ComandoWeb> comandos;
    private final ComandoWeb detalhe;
    private final ComandoWeb foto;
    private final ComandoWeb atualizarStatus;

    public Roteador(
            Map<ChaveRota, ComandoWeb> comandos,
            ComandoWeb detalhe,
            ComandoWeb foto
    ) {
        this(comandos, detalhe, foto, null);
    }

    public Roteador(
            Map<ChaveRota, ComandoWeb> comandos,
            ComandoWeb detalhe,
            ComandoWeb foto,
            ComandoWeb atualizarStatus
    ) {
        this.comandos = Map.copyOf(comandos);
        this.detalhe = detalhe;
        this.foto = foto;
        this.atualizarStatus = atualizarStatus;
    }

    public Optional<ComandoWeb> resolver(String metodo, String caminho) {
        ComandoWeb comando = comandos.get(
                new ChaveRota(metodo, caminho)
        );

        if (comando != null) {
            return Optional.of(comando);
        }

        if ("GET".equals(metodo) && caminho.startsWith("/itens/")) {
            return Optional.of(detalhe);
        }

        if ("GET".equals(metodo) && caminho.startsWith("/fotos/")) {
            return Optional.of(foto);
        }

        if (atualizarStatus != null
                && "POST".equals(metodo)
                && caminho.startsWith("/operacoes/itens/")
                && caminho.endsWith("/status")) {
            return Optional.of(atualizarStatus);
        }

        return Optional.empty();
    }
}