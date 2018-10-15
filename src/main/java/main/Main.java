package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.City;

public class Main {

    public static void main(String[] args) throws IOException {
        List<City> cities = findAllCities();

        Scanner input = new Scanner(System.in);
        System.out.print("Please enter the name of the city you are looking for: ");
        String name = input.next().toLowerCase();
        City city = null;

        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getName().toLowerCase().contains(name))
                city = cities.get(i);
        }

        if (city == null){
            System.out.println("Sorry, no such city found.");
        } else {
            System.out.format("Found city: %s. Here is the info found.\n", city.getName());
            System.out.println(city);
            System.out.println("Weather");
            System.out.println(city.getWeather());
        }


    }

    public static List<City> findAllCities() throws IOException {
        String url = "https://uk.wikipedia.org/wiki/%D0%9C%D1%96%D1%81%D1%82%D0%B0_%D0%A3%D0%BA%D1%80%D0%B0%D1%97%D0%BD%D0%B8_(%D0%B7%D0%B0_%D0%B0%D0%BB%D1%84%D0%B0%D0%B2%D1%96%D1%82%D0%BE%D0%BC)";
        Document doc = Jsoup.connect(url).get();
        Elements cities = doc.select("table tr");
        List<City> citiesList = new ArrayList<City>();

        for(int i = 1; i < cities.size(); i++) {
            Element city = cities.get(i);
            Elements properties = city.select("td");

            String name = properties.get(1).select("a").text();
            String cityUrl = properties.get(1).select("a").get(0).attr("href");
            String administrativeArea = properties.get(2).select("a").text();
            int numberOfCitizens = Integer.parseInt(properties.get(3).text().replaceAll("[^1234567890].*", ""));
            String yearOfFound = properties.get(4).text();
            double area = Double.parseDouble(properties.get(5).text());

            City cityObject = new City(name, cityUrl, administrativeArea, numberOfCitizens, yearOfFound, null, area);
            citiesList.add(cityObject);
        }

        return citiesList;
    }
}
