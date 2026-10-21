package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingServiceApp {
    public static List<Map<String, Object>> intersections;
    private static String firstInterId;
    private static String lastInterId;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections/{id}", ctx -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7022/intersections"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JSONArray jsonArray = new JSONArray(response.body());
            intersections = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                intersections.add(jsonArray.getJSONObject(i).toMap());
            }

            firstInterId = ctx.pathParam("id");


        });

        app.get("/intersections/{id}", ctx -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7022/intersections"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JSONArray jsonArray = new JSONArray(response.body());
            intersections = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                intersections.add(jsonArray.getJSONObject(i).toMap());
            }

            lastInterId = ctx.pathParam("id");

        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
