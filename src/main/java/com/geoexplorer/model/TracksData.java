package com.geoexplorer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wrapper usado pelo Jackson para deserializar o array raiz do tracks.json.
 */
public class TracksData {

    @JsonProperty("tracks")
    private List<Track> tracks;

    public TracksData() {}

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}
