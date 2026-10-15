package com.geoexplorer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representa um desafio de código associado a uma trilha e nível específico.
 */
public class Challenge {

    @JsonProperty("level")
    private String level;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("tips")
    private List<String> tips;

    // ── Construtores ──────────────────────────────────────────────────────────

    public Challenge() {}

    public Challenge(String level, String title, String description, List<String> tips) {
        this.level = level;
        this.title = title;
        this.description = description;
        this.tips = tips;
    }

    // ── Getters e Setters ─────────────────────────────────────────────────────

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }

    @Override
    public String toString() {
        return "Challenge{level='" + level + "', title='" + title + "'}";
    }
}
