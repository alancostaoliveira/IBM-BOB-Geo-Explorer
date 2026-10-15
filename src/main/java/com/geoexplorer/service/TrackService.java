package com.geoexplorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geoexplorer.model.Challenge;
import com.geoexplorer.model.Track;
import com.geoexplorer.model.TracksData;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável por carregar as trilhas do arquivo JSON e expor as operações
 * de consulta de trilha e geração de desafios.
 */
@Service
public class TrackService {

    private static final String TRACKS_JSON = "data/tracks.json";

    private List<Track> tracks;
    private final ObjectMapper objectMapper;

    public TrackService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Carrega o arquivo tracks.json ao inicializar o bean Spring.
     */
    @PostConstruct
    public void loadTracks() throws IOException {
        InputStream is = new ClassPathResource(TRACKS_JSON).getInputStream();
        TracksData data = objectMapper.readValue(is, TracksData.class);
        this.tracks = data.getTracks();
    }

    // ── Consulta de Trilha ────────────────────────────────────────────────────

    /**
     * Retorna um resumo formatado da trilha identificada pelo {@code trackId}.
     * Se a trilha não for encontrada, retorna uma mensagem de erro amigável.
     */
    public String findTrackDetails(String trackId) {
        Optional<Track> found = tracks.stream()
                .filter(t -> t.getId().equalsIgnoreCase(trackId))
                .findFirst();

        if (found.isEmpty()) {
            return buildNotFoundMessage(trackId);
        }

        return buildTrackSummary(found.get());
    }

    /**
     * Lista todas as trilhas disponíveis com ID, nome e nível.
     */
    public String listAllTracks() {
        if (tracks.isEmpty()) {
            return "Nenhuma trilha disponível no momento.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════╗\n");
        sb.append("║             TRILHAS DISPONÍVEIS — GEO-EXPLORER       ║\n");
        sb.append("╠══════════════════════════════════════════════════════╣\n");

        for (Track t : tracks) {
            sb.append(String.format("║  %-18s │ %-22s │ %s%n",
                    t.getId(), t.getName(), t.getLevel()));
        }

        sb.append("╚══════════════════════════════════════════════════════╝\n");
        sb.append("\nUse: track --id <id>  para ver os detalhes de uma trilha.\n");
        return sb.toString();
    }

    // ── Geração de Desafio ────────────────────────────────────────────────────

    /**
     * Gera um desafio formatado para a {@code technology} e {@code level} informados.
     * A busca por tecnologia é parcial (ignora maiúsculas/minúsculas).
     */
    public String generateChallenge(String technology, String level) {
        Optional<Track> trackOpt = tracks.stream()
                .filter(t -> t.getTechnology().equalsIgnoreCase(technology)
                          || t.getId().equalsIgnoreCase(technology))
                .findFirst();

        if (trackOpt.isEmpty()) {
            return "❌ Tecnologia '" + technology + "' não encontrada.\n"
                 + "Tecnologias disponíveis: "
                 + tracks.stream().map(Track::getTechnology).toList() + "\n";
        }

        Track track = trackOpt.get();

        Optional<Challenge> challengeOpt = track.getChallenges().stream()
                .filter(c -> c.getLevel().equalsIgnoreCase(level))
                .findFirst();

        if (challengeOpt.isEmpty()) {
            List<String> availableLevels = track.getChallenges().stream()
                    .map(Challenge::getLevel)
                    .toList();
            return "❌ Nível '" + level + "' não encontrado para a trilha '" + track.getName() + "'.\n"
                 + "Níveis disponíveis: " + availableLevels + "\n";
        }

        return buildChallengeSummary(track, challengeOpt.get());
    }

    // ── Acesso interno (usado por CertificateService) ─────────────────────────

    public Optional<Track> findById(String trackId) {
        return tracks.stream()
                .filter(t -> t.getId().equalsIgnoreCase(trackId))
                .findFirst();
    }

    public List<Track> getAllTracks() {
        return tracks;
    }

    // ── Formatação ────────────────────────────────────────────────────────────

    private String buildTrackSummary(Track t) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  📚 TRILHA: %-41s║%n", t.getName()));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  ID        : %-39s║%n", t.getId()));
        sb.append(String.format("║  Tecnologia: %-39s║%n", t.getTechnology()));
        sb.append(String.format("║  Nível     : %-39s║%n", t.getLevel()));
        sb.append(String.format("║  Duração   : %-34s sem. ║%n", t.getDurationWeeks()));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append("║  DESCRIÇÃO                                           ║\n");
        sb.append(wrapLine("║  ", t.getDescription(), 52));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append("║  MÓDULOS                                             ║\n");
        int idx = 1;
        for (String module : t.getModules()) {
            sb.append(String.format("║  %d. %-49s║%n", idx++, truncate(module, 49)));
        }
        sb.append("╚══════════════════════════════════════════════════════╝\n");
        sb.append("\nUse: challenge --tech " + t.getTechnology() + " --level <Iniciante|Intermediário|Avançado>\n");
        return sb.toString();
    }

    private String buildChallengeSummary(Track track, Challenge c) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  🎯 DESAFIO: %-40s║%n", truncate(c.getTitle(), 40)));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Trilha     : %-38s║%n", truncate(track.getName(), 38)));
        sb.append(String.format("║  Tecnologia : %-38s║%n", track.getTechnology()));
        sb.append(String.format("║  Nível      : %-38s║%n", c.getLevel()));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append("║  DESCRIÇÃO                                           ║\n");
        sb.append(wrapLine("║  ", c.getDescription(), 52));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append("║  DICAS                                               ║\n");
        for (String tip : c.getTips()) {
            sb.append(String.format("║  → %-49s║%n", truncate(tip, 49)));
        }
        sb.append("╚══════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    private String buildNotFoundMessage(String id) {
        List<String> ids = tracks.stream()
                .sorted(Comparator.comparing(Track::getId))
                .map(t -> "  • " + t.getId() + " (" + t.getName() + ")")
                .toList();
        return "❌ Trilha '" + id + "' não encontrada.\n\nTrilhas disponíveis:\n"
             + String.join("\n", ids) + "\n";
    }

    private String wrapLine(String prefix, String text, int maxWidth) {
        StringBuilder sb = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxWidth) {
                sb.append(String.format("%-" + (maxWidth + prefix.length()) + "s║%n",
                        prefix + line.toString().trim()));
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (!line.isEmpty()) {
            sb.append(String.format("%-" + (maxWidth + prefix.length()) + "s║%n",
                    prefix + line.toString().trim()));
        }
        return sb.toString();
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
