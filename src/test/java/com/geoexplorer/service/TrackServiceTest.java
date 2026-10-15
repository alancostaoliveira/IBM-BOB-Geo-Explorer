package com.geoexplorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link TrackService}.
 *
 * Utiliza um ObjectMapper real e carrega o tracks.json do classpath,
 * garantindo que a leitura e a lógica de negócio funcionem de ponta a ponta.
 */
@DisplayName("TrackService — testes unitários")
class TrackServiceTest {

    private TrackService trackService;

    @BeforeEach
    void setUp() throws Exception {
        trackService = new TrackService(new ObjectMapper());
        trackService.loadTracks();
    }

    // ── findTrackDetails ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar detalhes quando o ID da trilha existir")
    void shouldReturnTrackDetailsWhenIdExists() {
        String result = trackService.findTrackDetails("java-backend");

        assertNotNull(result);
        assertTrue(result.contains("Java Backend Developer"),
                "Deve conter o nome da trilha");
        assertTrue(result.contains("Spring Boot"),
                "Deve conter ao menos um módulo da trilha");
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro quando ID não existir")
    void shouldReturnErrorWhenTrackNotFound() {
        String result = trackService.findTrackDetails("tecnologia-inexistente");

        assertNotNull(result);
        assertTrue(result.contains("não encontrada"),
                "Deve informar que a trilha não foi encontrada");
    }

    @Test
    @DisplayName("A busca por ID deve ser case-insensitive")
    void shouldFindTrackCaseInsensitive() {
        String lowerResult = trackService.findTrackDetails("JAVA-BACKEND");
        String upperResult = trackService.findTrackDetails("java-backend");

        // Ambos devem retornar o mesmo conteúdo (sem mensagem de erro)
        assertFalse(lowerResult.contains("não encontrada"),
                "Busca com maiúsculas deve encontrar a trilha");
        assertEquals(upperResult, lowerResult,
                "O resultado deve ser idêntico independente do case");
    }

    // ── listAllTracks ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar todas as trilhas disponíveis")
    void shouldListAllTracks() {
        String result = trackService.listAllTracks();

        assertNotNull(result);
        assertTrue(result.contains("java-backend"),   "Deve listar a trilha Java");
        assertTrue(result.contains("angular-frontend"), "Deve listar a trilha Angular");
        assertTrue(result.contains("python-data"),    "Deve listar a trilha Python");
        assertTrue(result.contains("devops-cloud"),   "Deve listar a trilha DevOps");
    }

    // ── generateChallenge ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar desafio para tecnologia e nível válidos")
    void shouldReturnChallengeForValidTechAndLevel() {
        String result = trackService.generateChallenge("Java", "Iniciante");

        assertNotNull(result);
        assertTrue(result.contains("Calculadora"),
                "Deve conter o título do desafio iniciante de Java");
        assertTrue(result.contains("Iniciante"),
                "Deve indicar o nível correto");
    }

    @Test
    @DisplayName("Deve retornar desafio avançado de Python")
    void shouldReturnAdvancedPythonChallenge() {
        String result = trackService.generateChallenge("Python", "Avançado");

        assertNotNull(result);
        assertFalse(result.contains("não encontrad"),
                "Não deve retornar mensagem de 'não encontrado'");
        assertTrue(result.contains("Avançado"),
                "Deve indicar nível Avançado");
    }

    @Test
    @DisplayName("Deve retornar erro para tecnologia inexistente")
    void shouldReturnErrorForUnknownTechnology() {
        String result = trackService.generateChallenge("COBOL", "Iniciante");

        assertTrue(result.contains("não encontrad"),
                "Deve informar que a tecnologia não foi encontrada");
    }

    @Test
    @DisplayName("Deve retornar erro para nível inválido na trilha existente")
    void shouldReturnErrorForInvalidLevel() {
        String result = trackService.generateChallenge("Java", "Expert");

        assertTrue(result.contains("não encontrado") || result.contains("Níveis disponíveis"),
                "Deve informar que o nível não está disponível");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById deve retornar Optional preenchido para trilha existente")
    void shouldReturnOptionalForExistingTrack() {
        var result = trackService.findById("angular-frontend");

        assertTrue(result.isPresent(), "Deve encontrar a trilha Angular");
        assertEquals("angular-frontend", result.get().getId());
    }

    @Test
    @DisplayName("findById deve retornar Optional vazio para trilha inexistente")
    void shouldReturnEmptyOptionalForMissingTrack() {
        var result = trackService.findById("trilha-fantasma");

        assertTrue(result.isEmpty(), "Deve retornar Optional.empty()");
    }

    // ── getAllTracks ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTracks deve retornar lista com pelo menos 4 trilhas")
    void shouldReturnAtLeastFourTracks() {
        var all = trackService.getAllTracks();

        assertNotNull(all);
        assertTrue(all.size() >= 4,
                "O arquivo tracks.json deve conter no mínimo 4 trilhas");
    }
}
