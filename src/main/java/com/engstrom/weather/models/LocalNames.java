package com.engstrom.weather.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// This class is specifically ignored (for now) because it's not needed and not in utf-8
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalNames {
    public String ja;

    public String pl;

    public String ru;

    public String ta;

    public String uk;

    public String en;

    public String lt;
}
