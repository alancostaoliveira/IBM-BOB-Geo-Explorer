package com.geoexplorer.command;

import com.geoexplorer.service.CertificateService;
import com.geoexplorer.service.TrackService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Componente Spring Shell que expõe os três comandos principais do Geo-Explorer:
 * <ul>
 *   <li>{@code track}       — consulta o plano de estudos de uma trilha</li>
 *   <li>{@code challenge}   — gera um desafio de código por tecnologia e nível</li>
 *   <li>{@code certificate} — emite um certificado fictício de conclusão</li>
 * </ul>
 *
 * Para iniciar o shell interativo: {@code java -jar geo-explorer.jar}
 * Para modo não-interativo:        {@code java -jar geo-explorer.jar track --id java-backend}
 */
@ShellComponent
public class ExplorerCommands {

    private final TrackService trackService;
    private final CertificateService certificateService;

    public ExplorerCommands(TrackService trackService, CertificateService certificateService) {
        this.trackService = trackService;
        this.certificateService = certificateService;
    }

    // ── Comando: track ────────────────────────────────────────────────────────

    /**
     * Exibe o plano de estudos de uma trilha pelo seu ID.
     * Use {@code --list} para listar todas as trilhas disponíveis.
     *
     * Exemplos:
     * <pre>
     *   track --id java-backend
     *   track --list
     * </pre>
     */
    @ShellMethod(key = "track", value = "Consulta o plano de estudos de uma trilha pelo ID.")
    public String track(
            @ShellOption(value = {"-i", "--id"},
                         help = "ID da trilha (ex: java-backend, angular-frontend)",
                         defaultValue = ShellOption.NULL)
            String id,

            @ShellOption(value = {"-l", "--list"},
                         help = "Lista todas as trilhas disponíveis",
                         defaultValue = "false")
            boolean list) {

        if (list) {
            return trackService.listAllTracks();
        }

        if (id == null || id.isBlank()) {
            return "⚠️  Informe --id <id-da-trilha> ou use --list para ver as opções.\n";
        }

        return trackService.findTrackDetails(id);
    }

    // ── Comando: challenge ────────────────────────────────────────────────────

    /**
     * Gera um desafio de código para a tecnologia e nível informados.
     *
     * Exemplos:
     * <pre>
     *   challenge --tech Java --level Intermediário
     *   challenge --tech Python --level Avançado
     * </pre>
     */
    @ShellMethod(key = "challenge", value = "Gera um desafio de código por tecnologia e nível.")
    public String challenge(
            @ShellOption(value = {"-t", "--tech"},
                         help = "Tecnologia (ex: Java, Angular, Python, DevOps)")
            String tech,

            @ShellOption(value = {"-l", "--level"},
                         help = "Nível: Iniciante, Intermediário ou Avançado")
            String level) {

        return trackService.generateChallenge(tech, level);
    }

    // ── Comando: certificate ──────────────────────────────────────────────────

    /**
     * Emite um certificado fictício de conclusão de trilha para o aluno informado.
     *
     * Exemplos:
     * <pre>
     *   certificate --name "João Silva" --track java-backend
     *   certificate --name "Maria Souza" --track python-data
     * </pre>
     */
    @ShellMethod(key = "certificate", value = "Gera um certificado fictício de conclusão de trilha.")
    public String certificate(
            @ShellOption(value = {"-n", "--name"},
                         help = "Nome completo do aluno")
            String name,

            @ShellOption(value = {"-t", "--track"},
                         help = "ID da trilha concluída (ex: java-backend)")
            String track) {

        return certificateService.createCertificate(name, track);
    }

    // ── Comando: help-geo ─────────────────────────────────────────────────────

    /**
     * Exibe a ajuda rápida do Geo-Explorer com exemplos de uso de cada comando.
     */
    @ShellMethod(key = "help-geo", value = "Exibe a ajuda rápida do Geo-Explorer.")
    public String helpGeo() {
        return """
                \n
                ╔══════════════════════════════════════════════════════════════╗
                ║              🌍  GEO-EXPLORER — AJUDA RÁPIDA  🌍             ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  COMANDO: track                                              ║
                ║  Exibe o plano de estudos de uma trilha.                     ║
                ║                                                              ║
                ║    track --list                    Lista todas as trilhas    ║
                ║    track --id java-backend         Detalha a trilha          ║
                ║                                                              ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  COMANDO: challenge                                          ║
                ║  Gera um desafio de código por tecnologia e nível.           ║
                ║                                                              ║
                ║    challenge --tech Java --level Iniciante                   ║
                ║    challenge --tech Python --level Avançado                  ║
                ║                                                              ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  COMANDO: certificate                                        ║
                ║  Emite um certificado fictício de conclusão de trilha.       ║
                ║                                                              ║
                ║    certificate --name "João Silva" --track java-backend      ║
                ║                                                              ║
                ╚══════════════════════════════════════════════════════════════╝
                """;
    }
}
