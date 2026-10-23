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
import java.util.Set;

public class RoutingServiceApp {
    public static List<Map<String, Object>> intersections;
    public static String firstInterId;
    public static String lastInterId;
    public static int congestionLevel;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections/{id}", ctx -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7020/intersections/{id}"))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
                ctx.json(response.body());

        });



        app.get("/congestion", ctx -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7022/congestion"))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
                ctx.result(response.body());



            }catch(Exception e){
                ctx.status(503).result("CongestionServiceApp is unreachable");
            }

        });

        app.get("/route/{firstId}/{lastId}", ctx -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request1 = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7023/intersections/{id}"))
                    .GET()
                    .build();

            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7023/intersections/{id}"))
                    .GET()
                    .build();

            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

            firstInterId = response1.body();
            lastInterId = response2.body();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7023/congestion"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            congestionLevel = Integer.parseInt(response.body());

            Set<String> VALID_DISTRICTS = Set.of("Downtown", "Midtown", "Uptown", "Westside", "Eastside");

            for (Map<String, Object> inter : intersections) {
                String intersectionId = (String) inter.get("Intersection_id");
                if (firstInterId.equals(intersectionId) || lastInterId.equals(intersectionId)) {
                    String district = (String) inter.get("District");
                    if (!VALID_DISTRICTS.contains(district)) {
                        ctx.status(404).result("District not recognized");
                        return;
                    }
                    ctx.json(inter);
                    ctx.status(200);
                    return;
                }

            }



        });


    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
