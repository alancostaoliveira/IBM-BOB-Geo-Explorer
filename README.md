# 🌍 Geo-Explorer

> Plataforma de trilhas de estudo interativas com Spring Shell + MCP Server.  
> Projeto de portfólio desenvolvido como parte do desafio DIO.

---

## 📋 O que é o Geo-Explorer?

O **Geo-Explorer** é uma aplicação de linha de comando (CLI) construída com **Java 21**, **Spring Boot 3** e **Spring Shell**. Ela simula uma plataforma educacional com três comandos principais:

| Comando | O que faz |
|---|---|
| `track` | Apresenta o plano de estudos de uma trilha |
| `challenge` | Gera um desafio de código por tecnologia e nível |
| `certificate` | Emite um certificado fictício de conclusão |

Além da interface CLI, o projeto inclui um **Servidor MCP** (Model Context Protocol) em TypeScript, que expõe as mesmas funcionalidades como ferramentas integráveis a agentes de IA como o IBM Bob.

---

## 🏗️ Estrutura do Projeto

```text
geo-explorer/
├── src/
│   ├── main/
│   │   ├── java/com/geoexplorer/
│   │   │   ├── GeoExplorerApplication.java      # Entry point Spring Boot
│   │   │   ├── command/
│   │   │   │   └── ExplorerCommands.java         # Comandos Spring Shell
│   │   │   ├── model/
│   │   │   │   ├── Track.java                    # Modelo de trilha
│   │   │   │   ├── Challenge.java                # Modelo de desafio
│   │   │   │   └── TracksData.java               # Wrapper para deserialização JSON
│   │   │   └── service/
│   │   │       ├── TrackService.java             # Lógica de trilhas e desafios
│   │   │       └── CertificateService.java       # Geração de certificados
│   │   └── resources/
│   │       ├── data/tracks.json                  # Base de trilhas fictícias
│   │       └── application.yml
│   └── test/java/com/geoexplorer/
│       ├── service/TrackServiceTest.java
│       ├── service/CertificateServiceTest.java
│       └── command/ExplorerCommandsTest.java
├── mcp-server/                                   # Servidor MCP (TypeScript)
│   ├── src/index.ts
│   ├── package.json
│   └── tsconfig.json
├── certificates/                                 # Certificados gerados (saída)
├── pom.xml
└── README.md
```

---

## ⚙️ Pré-requisitos

- **Java 21+**
- **Maven 3.9+**
- **Node.js 20+** e **npm** (apenas para o MCP Server)

---

## 🚀 Como Executar o Projeto

### 1. Clone o repositório

```bash
git clone https://github.com/<seu-usuario>/geo-explorer.git
cd geo-explorer
```

### 2. Compile e inicie o Shell interativo

```bash
mvn spring-boot:run
```

Você verá o prompt interativo do Spring Shell:

```
  _____            _____          _
 / ____|          |  ___|        | |
| |  __  ___  ___ | |__  __  __ | |  ___  _ __  ___  _ __
| | |_ |/ _ \/ _ \|  __| \ \/ / | | / _ \| '__|/ _ \| '__|
| |__| |  __/ (_) | |___  >  <  | || (_) | |  | (_) | |
 \_____|\___|\___/\_____|/_/\_\ |_| \___/|_|   \___/|_|

geo-explorer:>
```

### 3. Ou execute como JAR (modo não-interativo)

```bash
mvn package -DskipTests
java -jar target/geo-explorer-1.0.0.jar track --id java-backend
```

---

## 🛠️ Como Usar os Comandos

### `track` — Consultar trilha de estudos

```bash
# Listar todas as trilhas disponíveis
geo-explorer:> track --list

# Ver detalhes de uma trilha específica
geo-explorer:> track --id java-backend
geo-explorer:> track --id angular-frontend
geo-explorer:> track --id python-data
geo-explorer:> track --id devops-cloud
```

**Saída de exemplo:**
```
╔══════════════════════════════════════════════════════╗
║  📚 TRILHA: Java Backend Developer                   ║
╠══════════════════════════════════════════════════════╣
║  ID        : java-backend                           ║
║  Tecnologia: Java                                   ║
║  Nível     : Intermediário                          ║
║  Duração   : 12 sem.                                ║
...
```

---

### `challenge` — Gerar desafio de código

```bash
# Parâmetros: --tech <tecnologia> --level <nível>
geo-explorer:> challenge --tech Java --level Iniciante
geo-explorer:> challenge --tech Python --level Avançado
geo-explorer:> challenge --tech Angular --level Intermediário
geo-explorer:> challenge --tech DevOps --level Iniciante
```

Níveis disponíveis: `Iniciante` | `Intermediário` | `Avançado`

---

### `certificate` — Gerar certificado fictício

```bash
# Parâmetros: --name "<nome>" --track <id-da-trilha>
geo-explorer:> certificate --name "João Silva" --track java-backend
geo-explorer:> certificate --name "Maria Souza" --track python-data
```

O certificado é exibido no terminal **e** salvo em `certificates/certificado_<CODIGO>_<nome>.txt`.

---

### `help-geo` — Ajuda rápida

```bash
geo-explorer:> help-geo
```

---

## 🧪 Como Executar os Testes

```bash
# Executa todos os testes
mvn test

# Executa com relatório de cobertura detalhado
mvn test -pl . --no-transfer-progress
```

Os testes cobrem:

| Classe de Teste | O que testa |
|---|---|
| `TrackServiceTest` | Busca de trilhas, listagem, geração de desafios, case-insensitive |
| `CertificateServiceTest` | Geração de certificado, persistência em arquivo, validação de inputs |
| `ExplorerCommandsTest` | Roteamento dos comandos, delegação para serviços (com Mockito) |

---

## 🤖 MCP Server

O MCP Server permite que agentes de IA (como IBM Bob) acessem o Geo-Explorer diretamente como ferramentas.

### Instalação e build

```bash
cd mcp-server
npm install
npm run build
```

### Ferramentas expostas

| Ferramenta | Descrição |
|---|---|
| `list_tracks` | Lista todas as trilhas disponíveis |
| `get_track` | Retorna detalhes de uma trilha pelo ID |
| `get_challenge` | Gera um desafio por tecnologia e nível |
| `create_certificate` | Emite um certificado fictício de conclusão |

### Configuração no IBM Bob (`mcp.json`)

```json
{
  "mcpServers": {
    "geo-explorer": {
      "command": "node",
      "args": ["/caminho/absoluto/geo-explorer/mcp-server/build/index.js"]
    }
  }
}
```

---

## 💡 Melhorias Realizadas

Além da proposta base, foram adicionadas as seguintes melhorias:

1. **Quatro trilhas completas** — Java, Angular, Python e DevOps, cada uma com 3 níveis de desafio (Iniciante, Intermediário, Avançado)
2. **Comando `track --list`** — listagem rápida de todas as trilhas, além da consulta por ID
3. **Comando `help-geo`** — painel de ajuda dedicado com exemplos de todos os comandos
4. **Certificados persistidos em arquivo** — além de exibir no terminal, cada certificado é salvo como `.txt` com código único (UUID)
5. **Busca case-insensitive** — `--id JAVA-BACKEND` e `--id java-backend` produzem o mesmo resultado
6. **MCP Server em TypeScript** — integração completa com o protocolo MCP com 4 ferramentas mapeadas
7. **Testes com `@TempDir`** — os testes de certificado usam diretório temporário do JUnit, sem poluir o projeto

---

## 📚 O que Aprendi Durante o Desafio

- **Spring Shell**: como criar CLIs interativas em Java com anotações simples, definindo parâmetros com `@ShellOption` e agrupando comandos com `@ShellComponent`
- **Jackson + ClassPathResource**: como carregar e deserializar arquivos JSON do classpath de forma idiomática no Spring
- **Protocolo MCP**: como expor ferramentas para agentes de IA usando o SDK `@modelcontextprotocol/sdk` com `registerTool` e transport stdio
- **Testes com `@TempDir`**: uso do suporte nativo do JUnit 5 para isolamento de testes que produzem arquivos
- **Mockito com `@ExtendWith`**: como testar a camada de comando isolada dos serviços reais

---

## 📁 Certificados Gerados

Os certificados são salvos em `certificates/` com o formato:

```
certificado_<CODIGO>_<nome_do_aluno>.txt
```

> ⚠️ A pasta `certificates/` está listada no `.gitignore` — não envie certificados para o repositório.

---

## 🔒 Segurança

Este projeto não contém senhas, tokens ou credenciais. O MCP Server não requer autenticação.  
Revise os arquivos antes de publicar e nunca envie informações sensíveis para o GitHub.

---

<p align="center">
  Feito com ☕ Java + Spring Boot | Desafio DIO — Geo-Explorer
</p>
