package main;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.util.Scanner;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class City {
    private static String BASEURL = "https://uk.wikipedia.org";

    private String name;
    private String url;
    private String administraniveArea;
    private int numberOfCitiezens;
    private String yearOfFound;
    private Coordinates coordinates;
    private double area;
    private final static int INFO_SIZE = 6;

    private float parseCoordinateString(String coordString) {
        Scanner stringScanner = new Scanner(coordString);
        stringScanner.useDelimiter("°");
        float coord = stringScanner.nextInt();
        stringScanner.skip(stringScanner.delimiter());

        stringScanner.useDelimiter("′");
        if (stringScanner.hasNextInt()){
            coord += (float) stringScanner.nextInt() / 60;
            stringScanner.skip(stringScanner.delimiter());
        }

        stringScanner.useDelimiter("″");
        if (stringScanner.hasNextInt()) {
            coord += (float) stringScanner.nextInt() / 3600;
        }

        if (coordString.toLowerCase().contains("д") || coordString.toLowerCase().contains("з")) {
            coord *= -1;
        }

        return coord;
    }

    public Coordinates getCoordinates() {
        if (coordinates == null) {
            Document doc = null;
            try {
                doc = Jsoup.connect(BASEURL + getUrl()).get();
                Elements latitudeEl = doc.getElementsByClass("latitude");
                Elements longitudeEl = doc.getElementsByClass("longitude");
                if (latitudeEl.size() > 0 && longitudeEl.size() > 0) {
                    coordinates = new Coordinates(
                            parseCoordinateString(latitudeEl.get(0).text()),
                            parseCoordinateString(longitudeEl.get(0).text()));
                }
            } catch(IOException e) { e.printStackTrace(); }
        }
        return coordinates;
    }

    public Weather getWeather() {
        return new Weather(getCoordinates());
    }


    @AllArgsConstructor
    @Getter
    class Coordinates {
        private final float latitude;
        private final float longitude;

        public String toString() {
            return "(lat: " + latitude + ", long: " + longitude + ")";
        }
    }

    @Getter
    private class Weather {
        private boolean found=false;
        private float temperatureC;
        private String conditionText;
        private float windKph;
        private String windDir;
        private float cloud;
        private static final String BASEURL = "https://api.apixu.com/v1/current.json";
        private static final String KEY = "c3c220f039b64720966123509181210";

        private Weather(Coordinates coordinates) {
            if (coordinates == null) {
                return;
            }

            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(BASEURL)
                        .queryString("key", KEY)
                        .queryString("q", coordinates.latitude + "," + coordinates.longitude)
                        .asJson();

                JSONObject result = jsonResponse.getBody().getObject();
                temperatureC = (float) result.getJSONObject("current").getDouble("temp_c");
                conditionText = result.getJSONObject("current").getJSONObject("condition").getString("text");
                windKph = (float) result.getJSONObject("current").getDouble("wind_kph");
                windDir = result.getJSONObject("current").getString("wind_dir");
                cloud = (float) result.getJSONObject("current").getDouble("cloud");
                found = true;
            } catch (UnirestException exception) { exception.printStackTrace(); }
        }

        public String toString() {
            if (found) {
                return String.format("Current temperature is: %.0f°C. %s. The wind is %.0fkph %s. Clouds: %.0f%%.",
                        temperatureC, conditionText, windKph, windDir, cloud);
            } else {
                return "The weather could not be retrieved.";
            }

        }
    }
}
