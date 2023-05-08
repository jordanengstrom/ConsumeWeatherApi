package com.engstrom.weather.models;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Rain {
    @JsonAlias({"3h"})
    public double $3h;
}
