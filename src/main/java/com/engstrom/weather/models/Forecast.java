package com.engstrom.weather.models;

import java.util.List;

public class Forecast {
    public String cod;
    public int message;
    public int cnt;
    public List<Datum> list;
    public City city;
}
