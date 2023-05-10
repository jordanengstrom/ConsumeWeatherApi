package com.engstrom.weather;

import com.engstrom.weather.models.Forecast;
import com.engstrom.weather.models.GeocodedCity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

@Controller
public class WeatherController {
    private final RestTemplate restTemplate;
    private static final Logger logger = LogManager.getLogger(WeatherController.class);

    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String city, @RequestParam String state, @RequestParam String country,
                         @RequestParam String units, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("city", city);
        redirectAttributes.addAttribute("state", state);
        redirectAttributes.addAttribute("country", country);
        redirectAttributes.addAttribute("units", units);
        return "redirect:/forecast";
    }

    @GetMapping("/forecast")
    public String forecast(@RequestParam String city, @RequestParam String state, @RequestParam String country,
                           @RequestParam String units, Model model) {
        GeocodedCity[] geocodedCities = GetGeocodedCities(city, state, country);

        // City array validation
        if (geocodedCities.length == 0) {
            String error =String.format("No cities found given the following search criteria - city: %s, state: %s, country: %s", city, state, country);
            logger.error(error);

            model.addAttribute("error", error);
            return "home";
        } else if (geocodedCities.length == 1) {
            try {
                // From file (use for local dev, testing):
                // forecast = ReadForecastDataFromFile();
                Forecast forecast = GetOpenWeatherMapForecast(geocodedCities[0].lat, geocodedCities[0].lon, units);

                model.addAttribute("country", country);
                model.addAttribute("state", state);
                model.addAttribute("units", units);
                model.addAttribute("forecast", forecast);

                return "forecast";
            } catch (Exception ex) {
                logger.fatal("Exception: " + ex);
                model.addAttribute("error", "Error occurred. Please try again.");
                return "home";
            }
        } else {
            String error = String.format("Multiple cities found given the following search criteria - city: %s, state: %s, country: %s", city, state, country);
            logger.error(error);

            model.addAttribute("error", error);
            return "home";
        }
    }

    //  helper methods
    private GeocodedCity[] GetGeocodedCities(String city, String state, String country) {
        try {
            // From file (use for local dev, testing):
            // geocodedCities = ReadGeocodedCitiesDataFromFile();
            return GetOpenWeatherMapGeocodedCities(city, state, country);
        } catch (Exception ex) {
            logger.fatal("Exception: " + ex);
            return new GeocodedCity[0];
        }
    }

    private Forecast GetOpenWeatherMapForecast(@RequestParam double latitude,
                                               @RequestParam double longitude,
                                               @RequestParam String units) {
        try {
            String baseUrl = "https://api.openweathermap.org/data/2.5/forecast";
            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("API_KEY");
            String requestUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=%s", baseUrl, latitude, longitude, apiKey, units);

            String loggedRequestUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=API_KEY&units=%s", baseUrl, latitude, longitude, units);
            logger.info("GET " + loggedRequestUrl);

            return restTemplate.getForObject(requestUrl, Forecast.class);
        } catch (RestClientException rce) {
            logger.fatal("RestClientException: " + rce);
            return new Forecast();
        }
    }

    private GeocodedCity[] GetOpenWeatherMapGeocodedCities(String city, String state, String country) {
        try {
            String baseUrl = "https://api.openweathermap.org/geo/1.0/direct";
            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("API_KEY");
            String requestUrl = String.format("%s?q=%s,%s,%s&appid=%s", baseUrl, city, state, country, apiKey);

            String loggedRequestUrl = String.format("%s?q=%s,%s,%s&appid=API_KEY", baseUrl, city, state, country);
            logger.info("GET " + loggedRequestUrl);

            return restTemplate.getForObject(requestUrl, GeocodedCity[].class);
        } catch (RestClientException rce) {
            logger.fatal("RestClientException: " + rce);
            return new GeocodedCity[0];
        }
    }

    private GeocodedCity[] ReadGeocodedCitiesDataFromFile() {
        try{
            File file = ResourceUtils.getFile("classpath:static/city.json");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, GeocodedCity[].class);
        } catch (IOException e) {
            logger.fatal("IOException: " + e);
            return new GeocodedCity[0];
        }
    }

    private Forecast ReadForecastDataFromFile() throws IOException {
        try {
            File file = ResourceUtils.getFile("classpath:static/data.json");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, Forecast.class);
        } catch (IOException e) {
            logger.fatal("IOException: " + e);
            return new Forecast();
        }
    }
}
