package com.engstrom.weather.models;

import java.util.List;

public class Response {
    public String cod;
    public int message;
    public int cnt;
    public List<DataPoint> list;
    public City city;
}
