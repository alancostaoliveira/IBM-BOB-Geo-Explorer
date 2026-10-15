#!/usr/bin/env node
/**
 * Geo-Explorer MCP Server
 *
 * Expõe as funcionalidades do Geo-Explorer como ferramentas MCP:
 *  - get_track       → retorna detalhes de uma trilha pelo ID
 *  - list_tracks     → lista todas as trilhas disponíveis
 *  - get_challenge   → retorna um desafio por tecnologia e nível
 *  - create_certificate → gera um certificado fictício de conclusão
 *
 * Transport: stdio (padrão MCP local)
 *
 * ⚠️ Use apenas console.error para logging — console.log corrompe o canal stdio do protocolo MCP.
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";
import { dirname, join } from "path";
import { randomUUID } from "crypto";

// ── Carregamento dos dados ────────────────────────────────────────────────────

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// O arquivo tracks.json está em ../../src/main/resources/data/ relativo ao build/
const TRACKS_PATH = join(__dirname, "../../src/main/resources/data/tracks.json");

interface Challenge {
  level: string;
  title: string;
  description: string;
  tips: string[];
}

interface Track {
  id: string;
  name: string;
  technology: string;
  level: string;
  durationWeeks: number;
  description: string;
  modules: string[];
  challenges: Challenge[];
}

interface TracksData {
  tracks: Track[];
}

function loadTracks(): Track[] {
  try {
    const raw = readFileSync(TRACKS_PATH, "utf-8");
    const data: TracksData = JSON.parse(raw);
    return data.tracks;
  } catch (err) {
    console.error("[geo-explorer-mcp] Erro ao carregar tracks.json:", err);
    return [];
  }
}

const tracks = loadTracks();
console.error(`[geo-explorer-mcp] ${tracks.length} trilhas carregadas.`);

// ── Servidor MCP ──────────────────────────────────────────────────────────────

const server = new McpServer({
  name: "geo-explorer",
  version: "1.0.0",
});

// ── Ferramenta: list_tracks ───────────────────────────────────────────────────

server.registerTool(
  "list_tracks",
  {
    description:
      "Lista todas as trilhas de estudo disponíveis no Geo-Explorer com ID, nome, tecnologia e nível.",
    inputSchema: z.object({}),
  },
  async () => {
    if (tracks.length === 0) {
      return {
        content: [{ type: "text", text: "Nenhuma trilha disponível." }],
        isError: true,
      };
    }

    const lines = tracks.map(
      (t) =>
        `• ${t.id.padEnd(20)} | ${t.name.padEnd(30)} | ${t.level.padEnd(15)} | ${t.durationWeeks} semanas`
    );

    return {
      content: [
        {
          type: "text",
          text: "TRILHAS DISPONÍVEIS — GEO-EXPLORER\n\n" + lines.join("\n"),
        },
      ],
    };
  }
);

// ── Ferramenta: get_track ─────────────────────────────────────────────────────

server.registerTool(
  "get_track",
  {
    description:
      "Retorna o plano de estudos completo de uma trilha, incluindo módulos e desafios disponíveis.",
    inputSchema: z.object({
      track_id: z
        .string()
        .describe(
          "ID da trilha (ex: java-backend, angular-frontend, python-data, devops-cloud)"
        ),
    }),
  },
  async ({ track_id }) => {
    const track = tracks.find(
      (t) => t.id.toLowerCase() === track_id.toLowerCase()
    );

    if (!track) {
      const available = tracks.map((t) => t.id).join(", ");
      return {
        content: [
          {
            type: "text",
            text: `Trilha '${track_id}' não encontrada.\nIDs disponíveis: ${available}`,
          },
        ],
        isError: true,
      };
    }

    const moduleList = track.modules
      .map((m, i) => `  ${i + 1}. ${m}`)
      .join("\n");
    const challengeLevels = track.challenges.map((c) => c.level).join(", ");

    const text = [
      `📚 TRILHA: ${track.name}`,
      ``,
      `ID          : ${track.id}`,
      `Tecnologia  : ${track.technology}`,
      `Nível       : ${track.level}`,
      `Duração     : ${track.durationWeeks} semanas`,
      ``,
      `DESCRIÇÃO`,
      track.description,
      ``,
      `MÓDULOS`,
      moduleList,
      ``,
      `DESAFIOS DISPONÍVEIS (níveis): ${challengeLevels}`,
    ].join("\n");

    return { content: [{ type: "text", text }] };
  }
);

// ── Ferramenta: get_challenge ─────────────────────────────────────────────────

server.registerTool(
  "get_challenge",
  {
    description:
      "Retorna um desafio de código para uma tecnologia e nível específicos.",
    inputSchema: z.object({
      technology: z
        .string()
        .describe(
          "Tecnologia do desafio (ex: Java, Angular, Python, DevOps)"
        ),
      level: z
        .string()
        .describe("Nível de dificuldade: Iniciante, Intermediário ou Avançado"),
    }),
  },
  async ({ technology, level }) => {
    const track = tracks.find(
      (t) =>
        t.technology.toLowerCase() === technology.toLowerCase() ||
        t.id.toLowerCase() === technology.toLowerCase()
    );

    if (!track) {
      const techs = tracks.map((t) => t.technology).join(", ");
      return {
        content: [
          {
            type: "text",
            text: `Tecnologia '${technology}' não encontrada.\nDisponíveis: ${techs}`,
          },
        ],
        isError: true,
      };
    }

    const challenge = track.challenges.find(
      (c) => c.level.toLowerCase() === level.toLowerCase()
    );

    if (!challenge) {
      const levels = track.challenges.map((c) => c.level).join(", ");
      return {
        content: [
          {
            type: "text",
            text: `Nível '${level}' não disponível para ${track.name}.\nNíveis disponíveis: ${levels}`,
          },
        ],
        isError: true,
      };
    }

    const tipsList = challenge.tips.map((t) => `  → ${t}`).join("\n");

    const text = [
      `🎯 DESAFIO: ${challenge.title}`,
      ``,
      `Trilha      : ${track.name}`,
      `Tecnologia  : ${track.technology}`,
      `Nível       : ${challenge.level}`,
      ``,
      `DESCRIÇÃO`,
      challenge.description,
      ``,
      `DICAS`,
      tipsList,
    ].join("\n");

    return { content: [{ type: "text", text }] };
  }
);

// ── Ferramenta: create_certificate ───────────────────────────────────────────

server.registerTool(
  "create_certificate",
  {
    description:
      "Gera um certificado fictício de conclusão de trilha para um aluno. Retorna o conteúdo do certificado formatado.",
    inputSchema: z.object({
      student_name: z
        .string()
        .min(2)
        .describe("Nome completo do aluno que concluiu a trilha"),
      track_id: z
        .string()
        .describe("ID da trilha concluída (ex: java-backend)"),
    }),
  },
  async ({ student_name, track_id }) => {
    const track = tracks.find(
      (t) => t.id.toLowerCase() === track_id.toLowerCase()
    );

    if (!track) {
      const available = tracks.map((t) => t.id).join(", ");
      return {
        content: [
          {
            type: "text",
            text: `Trilha '${track_id}' não encontrada.\nIDs disponíveis: ${available}`,
          },
        ],
        isError: true,
      };
    }

    const certId = randomUUID().substring(0, 8).toUpperCase();
    const issueDate = new Date().toLocaleDateString("pt-BR");

    const text = [
      ``,
      `╔══════════════════════════════════════════════════════════════╗`,
      `║               🌍  GEO-EXPLORER ACADEMY  🌍                  ║`,
      `║                  CERTIFICADO DE CONCLUSÃO                   ║`,
      `╠══════════════════════════════════════════════════════════════╣`,
      `║                                                              ║`,
      `║  Certificamos que:                                           ║`,
      `║    » ${student_name.padEnd(56)}║`,
      `║                                                              ║`,
      `║  Concluiu com êxito a trilha:                                ║`,
      `║    » ${track.name.padEnd(56)}║`,
      `║                                                              ║`,
      `║  Tecnologia : ${track.technology.padEnd(47)}║`,
      `║  Nível      : ${track.level.padEnd(47)}║`,
      `║  Duração    : ${String(track.durationWeeks + " semanas").padEnd(47)}║`,
      `║                                                              ║`,
      `╠══════════════════════════════════════════════════════════════╣`,
      `║  Data de Emissão : ${issueDate.padEnd(42)}║`,
      `║  Código          : ${certId.padEnd(42)}║`,
      `║  Assinatura      : GEO-EXPLORER ACADEMY                     ║`,
      `║                                                              ║`,
      `║  * Este é um certificado fictício para fins de portfólio.   ║`,
      `╚══════════════════════════════════════════════════════════════╝`,
    ].join("\n");

    return { content: [{ type: "text", text }] };
  }
);

// ── Inicialização ─────────────────────────────────────────────────────────────

async function main(): Promise<void> {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("[geo-explorer-mcp] Servidor MCP em execução via stdio.");
}

main().catch((error) => {
  console.error("[geo-explorer-mcp] Erro fatal:", error);
  process.exit(1);
});
