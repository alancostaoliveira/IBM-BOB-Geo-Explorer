package com.geoexplorer.service;

import com.geoexplorer.model.Track;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável por gerar certificados fictícios de conclusão de trilha.
 * Os certificados são salvos como arquivos .txt na pasta configurada.
 */
@Service
public class CertificateService {

    @Value("${geo.explorer.certificates-output-dir:certificates/}")
    private String outputDir;

    private final TrackService trackService;

    public CertificateService(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Cria um certificado fictício para o aluno {@code studentName} na trilha {@code trackId}.
     * Salva o certificado em arquivo e retorna o conteúdo formatado.
     *
     * @param studentName nome completo do aluno
     * @param trackId     identificador da trilha concluída
     * @return certificado formatado como String
     */
    public String createCertificate(String studentName, String trackId) {
        Optional<Track> trackOpt = trackService.findById(trackId);

        if (trackOpt.isEmpty()) {
            return "❌ Trilha '" + trackId + "' não encontrada. Verifique o ID com o comando: track --list\n";
        }

        if (studentName == null || studentName.isBlank()) {
            return "❌ Por favor, informe um nome válido para o certificado.\n";
        }

        Track track = trackOpt.get();
        String certificateId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String issueDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String content = buildCertificateContent(studentName, track, certificateId, issueDate);

        String savedPath = saveCertificate(certificateId, studentName, content);

        return content + "\n📁 Certificado salvo em: " + savedPath + "\n";
    }

    // ── Formatação ────────────────────────────────────────────────────────────

    private String buildCertificateContent(String name, Track track,
                                            String certId, String date) {
        return """
                \n
                ╔══════════════════════════════════════════════════════════════╗
                ║                                                              ║
                ║               🌍  GEO-EXPLORER ACADEMY  🌍                  ║
                ║                                                              ║
                ║                  CERTIFICADO DE CONCLUSÃO                   ║
                ║                                                              ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  Certificamos que                                            ║
                ║                                                              ║
                ║    » %s
                ║                                                              ║
                ║  concluiu com êxito a trilha de estudos:                    ║
                ║                                                              ║
                ║    » %s
                ║                                                              ║
                ║  Tecnologia : %-47s║
                ║  Nível      : %-47s║
                ║  Duração    : %-42s sem.  ║
                ║                                                              ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  Data de Emissão : %-42s║
                ║  Código          : %-42s║
                ║                                                              ║
                ║  Assinatura Digital: GEO-EXPLORER ACADEMY                   ║
                ║  Este é um certificado fictício gerado para fins de         ║
                ║  estudo e desenvolvimento de portfólio.                     ║
                ║                                                              ║
                ╚══════════════════════════════════════════════════════════════╝
                """.formatted(
                padRight(name, 54),
                padRight(track.getName(), 54),
                track.getTechnology(),
                track.getLevel(),
                track.getDurationWeeks(),
                date,
                certId
        );
    }

    // ── Persistência ──────────────────────────────────────────────────────────

    private String saveCertificate(String certId, String studentName, String content) {
        try {
            Path dir = Path.of(outputDir);
            Files.createDirectories(dir);

            String fileName = "certificado_" + certId + "_"
                    + sanitize(studentName) + ".txt";
            Path filePath = dir.resolve(fileName);
            Files.writeString(filePath, content);
            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            return "(não foi possível salvar o arquivo: " + e.getMessage() + ")";
        }
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private String padRight(String text, int length) {
        if (text.length() >= length) return text.substring(0, length);
        return text + " ".repeat(length - text.length());
    }

    private String sanitize(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9]", "_")
                   .replaceAll("_+", "_");
    }
}
