package br.ucsal.voltapramim.infraestrutura.eventos;

import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditoriaObserverTest {
    @Test
    void registraTipoEIdentificadorDoEventoNoLog() {
        Logger logger = Logger.getLogger(AuditoriaObserver.class.getName());
        List<String> mensagens = new ArrayList<>();
        Handler captura = new Handler() {
            @Override public void publish(LogRecord registro) { mensagens.add(registro.getMessage()); }
            @Override public void flush() { }
            @Override public void close() { }
        };
        Level nivelOriginal = logger.getLevel();
        boolean usavaPai = logger.getUseParentHandlers();
        logger.addHandler(captura);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        try {
            UUID id = UUID.randomUUID();

            new AuditoriaObserver().aoReceber(new ItemRecebido(id, Instant.now()));

            assertEquals(1, mensagens.size());
            assertTrue(mensagens.get(0).contains("ItemRecebido"));
            assertTrue(mensagens.get(0).contains(id.toString()));
        } finally {
            logger.removeHandler(captura);
            logger.setLevel(nivelOriginal);
            logger.setUseParentHandlers(usavaPai);
        }
    }
}
