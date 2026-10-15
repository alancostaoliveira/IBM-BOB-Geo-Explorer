package com.geoexplorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link CertificateService}.
 *
 * Usa um diretório temporário para não poluir o projeto durante os testes.
 */
@DisplayName("CertificateService — testes unitários")
class CertificateServiceTest {

    @TempDir
    Path tempDir;

    private CertificateService certificateService;

    @BeforeEach
    void setUp() throws Exception {
        TrackService trackService = new TrackService(new ObjectMapper());
        trackService.loadTracks();
        certificateService = new CertificateService(trackService);

        // Injeta o diretório temporário via reflexão (campo privado anotado com @Value)
        var field = CertificateService.class.getDeclaredField("outputDir");
        field.setAccessible(true);
        field.set(certificateService, tempDir.toString() + "/");
    }

    // ── createCertificate ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve gerar certificado para aluno e trilha válidos")
    void shouldCreateCertificateForValidInput() {
        String result = certificateService.createCertificate("Ana Costa", "java-backend");

        assertNotNull(result);
        assertTrue(result.contains("Ana Costa"),
                "Certificado deve conter o nome do aluno");
        assertTrue(result.contains("Java Backend Developer"),
                "Certificado deve conter o nome da trilha");
        assertTrue(result.contains("GEO-EXPLORER ACADEMY"),
                "Certificado deve conter o nome da academia");
    }

    @Test
    @DisplayName("Deve salvar o arquivo de certificado no diretório configurado")
    void shouldSaveCertificateFile() throws IOException {
        certificateService.createCertificate("Carlos Mendes", "python-data");

        long fileCount = Files.list(tempDir).count();
        assertEquals(1, fileCount, "Deve existir exatamente 1 arquivo de certificado");

        // Verifica que o nome do arquivo começa com "certificado_"
        String fileName = Files.list(tempDir).findFirst().get().getFileName().toString();
        assertTrue(fileName.startsWith("certificado_"),
                "O arquivo deve começar com 'certificado_'");
    }

    @Test
    @DisplayName("Certificado salvo deve conter o conteúdo do certificado")
    void savedFileShouldContainCertificateContent() throws IOException {
        certificateService.createCertificate("Luiza Pereira", "angular-frontend");

        Path certFile = Files.list(tempDir).findFirst().orElseThrow();
        String fileContent = Files.readString(certFile);

        assertTrue(fileContent.contains("Luiza Pereira"),
                "O arquivo salvo deve conter o nome do aluno");
        assertTrue(fileContent.contains("Angular Frontend Developer"),
                "O arquivo salvo deve conter o nome da trilha");
    }

    @Test
    @DisplayName("Deve retornar erro para trilha inexistente")
    void shouldReturnErrorForNonExistentTrack() {
        String result = certificateService.createCertificate("Marcos", "trilha-fantasma");

        assertTrue(result.contains("não encontrada"),
                "Deve informar que a trilha não foi encontrada");
    }

    @Test
    @DisplayName("Deve retornar erro para nome de aluno em branco")
    void shouldReturnErrorForBlankStudentName() {
        String result = certificateService.createCertificate("   ", "java-backend");

        assertTrue(result.contains("nome válido"),
                "Deve solicitar um nome válido");
    }

    @Test
    @DisplayName("Cada certificado deve ter um código único")
    void eachCertificateShouldHaveUniqueCode() throws IOException {
        certificateService.createCertificate("Alice", "devops-cloud");
        certificateService.createCertificate("Bob", "devops-cloud");

        long count = Files.list(tempDir).count();
        assertEquals(2, count, "Devem existir 2 arquivos de certificado distintos");
    }
}
