package com.engstrom.weather.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodedCity {
    public String name;
    @JsonIgnore
    public LocalNames local_names;
    public double lat;
    public double lon;
    public String country;
    public String state;
}
