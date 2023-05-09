package com.engstrom.weather;

import com.engstrom.weather.models.Forecast;
import com.engstrom.weather.models.GeocodedCity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
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
    public String forecast(@RequestParam String city,
                           @RequestParam String state,
                           @RequestParam String country,
                           @RequestParam String units,
                           Model model) {

        GeocodedCity geocodedCity = GetGeocodedCity(city, state, country);
        Forecast forecast = null;
        try {
            // From file (use for local dev, testing):
            // forecast = ReadForecastDataFromFile();
             assert geocodedCity != null;
             forecast = GetOpenWeatherMapForecast(geocodedCity.lat, geocodedCity.lon, units);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        System.out.println("UNITS: " + units);
        model.addAttribute("state", state);
        model.addAttribute("units", units);
        model.addAttribute("forecast", forecast);
        return "forecast";
    }

    //  helper methods
    private GeocodedCity GetGeocodedCity(String city, String state, String country) {
        GeocodedCity[] geocodedCities = null;
        try {
            // From file (use for local dev, testing):
            // geocodedCities = ReadGeocodedCitiesDataFromFile();
            geocodedCities = GetOpenWeatherMapGeocodedCities(city, state, country);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        assert geocodedCities != null;
        return geocodedCities[0];
    }

    private Forecast GetOpenWeatherMapForecast(@RequestParam double latitude,
                                               @RequestParam double longitude,
                                               @RequestParam String units) throws RestClientException {
        String baseUrl = "https://api.openweathermap.org/data/2.5/forecast";
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("API_KEY");
        String requestUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=%s", baseUrl, latitude, longitude, apiKey, units);
        return restTemplate.getForObject(requestUrl, Forecast.class);
    }

    private GeocodedCity[] GetOpenWeatherMapGeocodedCities(String city, String state, String country) throws RestClientException {
        String baseUrl = "https://api.openweathermap.org/geo/1.0/direct";
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("API_KEY");
        String requestUrl = String.format("%s?q=%s,%s,%s&appid=%s", baseUrl, city, state, country, apiKey);
        return restTemplate.getForObject(requestUrl, GeocodedCity[].class);
    }

    private GeocodedCity[] ReadGeocodedCitiesDataFromFile() throws IOException {
        File file = ResourceUtils.getFile("classpath:static/city.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, GeocodedCity[].class);
    }

    private Forecast ReadForecastDataFromFile() throws IOException {
        File file = ResourceUtils.getFile("classpath:static/data.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, Forecast.class);
    }
}
