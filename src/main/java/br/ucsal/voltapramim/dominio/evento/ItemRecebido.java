package br.ucsal.voltapramim.dominio.evento;

import java.time.Instant;
import java.util.UUID;

public record ItemRecebido(UUID itemId, Instant ocorridoEm) implements EventoDominio { }