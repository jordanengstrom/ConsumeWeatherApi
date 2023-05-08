package com.engstrom.weather;

import com.engstrom.weather.models.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@RestController
public class OpenWeatherMapController {
    @Autowired
    private RestTemplate restTemplate;
    public Response ReadResponseDataFromFile() throws IOException {
        File file = ResourceUtils.getFile("classpath:static/data.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, Response.class);
    }

    public Response ReadResponseDataFromApiCall() throws RestClientException {
        String baseUrl = "https://api.openweathermap.org/data/2.5/forecast";
        double latitude = 43.6166163;
        double longitude = -116.200886;
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("API_KEY");
        String units = "imperial";
        String requestUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=%s", baseUrl, latitude, longitude, apiKey, units);
        return restTemplate.getForObject(requestUrl, Response.class);
    }

    @GetMapping("/forecast")
    public Response getForecast(){
        Response response = null;
        try {
            // From file:
            // response = ReadResponseDataFromFile();
            // From API request:
            response = ReadResponseDataFromApiCall();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return response;
    }
}
