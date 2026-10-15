package com.geoexplorer.command;

import com.geoexplorer.service.CertificateService;
import com.geoexplorer.service.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ExplorerCommands}.
 *
 * Utiliza Mockito para isolar os comandos dos serviços, verificando apenas
 * que o roteamento e as mensagens de validação dos comandos estão corretos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExplorerCommands — testes unitários")
class ExplorerCommandsTest {

    @Mock
    private TrackService trackService;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private ExplorerCommands explorerCommands;

    // ── Comando track ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("track --list deve chamar listAllTracks()")
    void trackListShouldCallListAllTracks() {
        when(trackService.listAllTracks()).thenReturn("lista de trilhas");

        String result = explorerCommands.track(null, true);

        verify(trackService, times(1)).listAllTracks();
        assertEquals("lista de trilhas", result);
    }

    @Test
    @DisplayName("track --id deve chamar findTrackDetails() com o ID correto")
    void trackIdShouldCallFindTrackDetails() {
        when(trackService.findTrackDetails("java-backend")).thenReturn("detalhes da trilha");

        String result = explorerCommands.track("java-backend", false);

        verify(trackService, times(1)).findTrackDetails("java-backend");
        assertEquals("detalhes da trilha", result);
    }

    @Test
    @DisplayName("track sem --id nem --list deve retornar aviso de uso")
    void trackWithoutArgsShouldReturnUsageWarning() {
        String result = explorerCommands.track(null, false);

        assertTrue(result.contains("--id") || result.contains("--list"),
                "Deve sugerir o uso de --id ou --list");
        verifyNoInteractions(trackService);
    }

    @Test
    @DisplayName("track com ID em branco deve retornar aviso de uso")
    void trackWithBlankIdShouldReturnWarning() {
        String result = explorerCommands.track("  ", false);

        assertTrue(result.contains("--id") || result.contains("--list"));
        verifyNoInteractions(trackService);
    }

    // ── Comando challenge ─────────────────────────────────────────────────────

    @Test
    @DisplayName("challenge deve chamar generateChallenge() com tech e level corretos")
    void challengeShouldDelegateToService() {
        when(trackService.generateChallenge("Java", "Iniciante")).thenReturn("desafio iniciante Java");

        String result = explorerCommands.challenge("Java", "Iniciante");

        verify(trackService, times(1)).generateChallenge("Java", "Iniciante");
        assertEquals("desafio iniciante Java", result);
    }

    // ── Comando certificate ───────────────────────────────────────────────────

    @Test
    @DisplayName("certificate deve chamar createCertificate() com nome e trilha corretos")
    void certificateShouldDelegateToService() {
        when(certificateService.createCertificate("João Silva", "java-backend"))
                .thenReturn("certificado gerado");

        String result = explorerCommands.certificate("João Silva", "java-backend");

        verify(certificateService, times(1)).createCertificate("João Silva", "java-backend");
        assertEquals("certificado gerado", result);
    }

    // ── Comando help-geo ──────────────────────────────────────────────────────

    @Test
    @DisplayName("help-geo deve retornar conteúdo de ajuda com os três comandos")
    void helpGeoShouldReturnHelpContent() {
        String result = explorerCommands.helpGeo();

        assertTrue(result.contains("track"),       "Deve mencionar o comando track");
        assertTrue(result.contains("challenge"),   "Deve mencionar o comando challenge");
        assertTrue(result.contains("certificate"), "Deve mencionar o comando certificate");
    }
}
