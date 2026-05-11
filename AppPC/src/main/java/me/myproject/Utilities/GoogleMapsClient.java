package me.myproject.Utilities;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoogleMapsClient {
    private final String apiKey;

    public GoogleMapsClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> autocomplete(String input) throws IOException {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8);
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + encoded + "&language=vi&key=" + apiKey;
        Map<String, Object> response = APIHelper.getForMap(url);
        Object predictionsObj = response.get("predictions");
        if (!(predictionsObj instanceof List)) {
            return List.of();
        }
        List<?> predictions = (List<?>) predictionsObj;
        List<String> results = new ArrayList<>();
        for (Object item : predictions) {
            if (item instanceof Map) {
                Object description = ((Map<?, ?>) item).get("description");
                if (description != null) {
                    results.add(description.toString());
                }
            }
        }
        return results;
    }

    public double getDistanceKm(String origin, String destination) throws IOException {
        String encodedOrigin = URLEncoder.encode(origin, StandardCharsets.UTF_8);
        String encodedDestination = URLEncoder.encode(destination, StandardCharsets.UTF_8);
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + encodedOrigin +
                "&destinations=" + encodedDestination + "&language=vi&key=" + apiKey;
        Map<String, Object> response = APIHelper.getForMap(url);
        Object rowsObj = response.get("rows");
        if (!(rowsObj instanceof List)) {
            return 0.0;
        }
        List<?> rows = (List<?>) rowsObj;
        if (rows.isEmpty() || !(rows.get(0) instanceof Map)) {
            return 0.0;
        }
        Object elementsObj = ((Map<?, ?>) rows.get(0)).get("elements");
        if (!(elementsObj instanceof List)) {
            return 0.0;
        }
        List<?> elements = (List<?>) elementsObj;
        if (elements.isEmpty() || !(elements.get(0) instanceof Map)) {
            return 0.0;
        }
        Object distanceObj = ((Map<?, ?>) elements.get(0)).get("distance");
        if (!(distanceObj instanceof Map)) {
            return 0.0;
        }
        Object valueObj = ((Map<?, ?>) distanceObj).get("value");
        if (!(valueObj instanceof Number)) {
            return 0.0;
        }
        double meters = ((Number) valueObj).doubleValue();
        return meters / 1000.0;
    }
}
