package com.geoexplorer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representa uma trilha de estudos com módulos e desafios associados.
 */
public class Track {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("technology")
    private String technology;

    @JsonProperty("level")
    private String level;

    @JsonProperty("durationWeeks")
    private int durationWeeks;

    @JsonProperty("description")
    private String description;

    @JsonProperty("modules")
    private List<String> modules;

    @JsonProperty("challenges")
    private List<Challenge> challenges;

    // ── Construtores ──────────────────────────────────────────────────────────

    public Track() {}

    // ── Getters e Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getModules() { return modules; }
    public void setModules(List<String> modules) { this.modules = modules; }

    public List<Challenge> getChallenges() { return challenges; }
    public void setChallenges(List<Challenge> challenges) { this.challenges = challenges; }

    @Override
    public String toString() {
        return "Track{id='" + id + "', name='" + name + "', level='" + level + "'}";
    }
}
