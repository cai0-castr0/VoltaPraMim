package br.ucsal.voltapramim.dominio.evento;

import java.time.Instant;
import java.util.UUID;

public record ItemDevolvido(UUID itemId, Instant ocorridoEm) implements EventoDominio { }