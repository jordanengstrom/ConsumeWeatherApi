package com.engstrom.weather.models;

import java.util.List;

public class Datum {
    public long dt;
    public Main main;
    public List<Weather> weather;
    public Clouds clouds;
    public Wind wind;
    public int visibility;
    public double pop;
    public Rain rain;
    public Sys sys;
    public String dt_txt;

}
