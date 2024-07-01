package pacote.controle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class TwentySevenThreads {

    private static final String BASE_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final String START_DATE = "2024-01-01";
    private static final String END_DATE = "2024-01-31";
    private static final String PARAMETERS = "temperature_2m_max,temperature_2m_min";

    private static class City {
        String name;
        String latitude;
        String longitude;

        City(String name, String latitude, String longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static class WeatherTask implements Runnable {
        private City city;

        WeatherTask(City city) {
            this.city = city;
        }

        @Override
        public void run() {
            getWeatherData(city);
        }

        private void getWeatherData(City city) {
            try {
                String urlString = BASE_URL + "?latitude=" + city.latitude + "&longitude=" + city.longitude +
                        "&start_date=" + START_DATE + "&end_date=" + END_DATE + "&daily=" + PARAMETERS;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                JSONObject json = new JSONObject(content.toString());
                JSONArray maxTempArray = json.getJSONObject("daily").getJSONArray("temperature_2m_max");
                JSONArray minTempArray = json.getJSONObject("daily").getJSONArray("temperature_2m_min");
                JSONArray datesArray = json.getJSONObject("daily").getJSONArray("time");

                System.out.println("Temperaturas diárias em " + city.name + " em janeiro de 2024:");

                for (int i = 0; i < datesArray.length(); i++) {
                    String date = datesArray.getString(i);
                    double maxTemp = maxTempArray.getDouble(i);
                    double minTemp = minTempArray.getDouble(i);
                    double avgTemp = (maxTemp + minTemp) / 2;

                    System.out.println("Data: " + date);
                    System.out.println("Temperatura média: " + String.format("%.1f", avgTemp) + "°C");
                    System.out.println("Temperatura mínima: " + minTemp + "°C");
                    System.out.println("Temperatura máxima: " + maxTemp + "°C");
                    System.out.println();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        City[] cities = {
            new City("Aracaju", "-10.9167", "-37.05"),
            new City("Belém", "-1.4558", "-48.5039"),
            new City("Belo Horizonte", "-19.9167", "-43.9333"),
            new City("Boa Vista", "2.81972", "-60.67333"),
            new City("Brasília", "-15.7939", "-47.8828"),
            new City("Campo Grande", "-20.44278", "-54.64639"),
            new City("Cuiabá", "-15.5989", "-56.0949"),
            new City("Curitiba", "-25.4297", "-49.2711"),
            new City("Florianópolis", "-27.5935", "-48.55854"),
            new City("Fortaleza", "-3.7275", "-38.5275"),
            new City("Goiânia", "-16.6667", "-49.25"),
            new City("João Pessoa", "-7.12", "-34.88"),
            new City("Macapá", "0.033", "-51.05"),
            new City("Maceió", "-9.66583", "-35.73528"),
            new City("Manaus", "-3.1189", "-60.0217"),
            new City("Natal", "-5.7833", "-35.2"),
            new City("Palmas", "-10.16745", "-48.32766"),
            new City("Porto Alegre", "-30.0331", "-51.23"),
            new City("Porto Velho", "-8.76194", "-63.90389"),
            new City("Recife", "-8.05", "-34.9"),
            new City("Rio Branco", "-9.97472", "-67.81"),
            new City("Rio de Janeiro", "-22.9111", "-43.2056"),
            new City("Salvador", "-12.9747", "-38.4767"),
            new City("São Luís", "-2.5283", "-44.3044"),
            new City("São Paulo", "-23.55", "-46.6333"),
            new City("Teresina", "-5.08917", "-42.80194"),
            new City("Vitória", "-20.2889", "-40.3083")
        };

        int repetitions = 10;
        long totalTime = 0;

        for (int j = 0; j < repetitions; j++) {
            long startTime = System.currentTimeMillis();

            Thread[] threads = new Thread[cities.length];
            for (int i = 0; i < cities.length; i++) {
                threads[i] = new Thread(new WeatherTask(cities[i]));
                threads[i].start();
            }

            for (int i = 0; i < cities.length; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        System.out.println("Tempo médio de execução: " + (totalTime / repetitions) + " ms");
    }
}
