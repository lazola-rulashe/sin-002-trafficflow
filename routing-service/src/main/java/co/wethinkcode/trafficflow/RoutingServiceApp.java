package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RoutingServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/route/{firstId}/{lastId}", ctx -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                // Validate first intersection via intersection-service
                HttpRequest request1 = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7021/intersections/" + ctx.pathParam("firstId")))
                        .GET()
                        .build();

                // Validate last intersection via intersection-service
                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7021/intersections/" + ctx.pathParam("lastId")))
                        .GET()
                        .build();

                HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
                HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

                // Check if both intersections exist
                if (response1.statusCode() != 200) {
                    ctx.status(404).result("First intersection not found");
                    return;
                }
                if (response2.statusCode() != 200) {
                    ctx.status(404).result("Last intersection not found");
                    return;
                }

                // Get current congestion level from congestion-service
                HttpRequest congestionRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7022/congestion"))
                        .GET()
                        .build();

                HttpResponse<String> congestionResponse = client.send(
                        congestionRequest,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (congestionResponse.statusCode() != 200) {
                    ctx.status(500).result("Unable to get congestion level");
                    return;
                }

                JSONObject congestionJson = new JSONObject(congestionResponse.body());
                int congestionLevel = congestionJson.getInt("level");

                // Calculate estimated travel time based on congestion level
                // Base time: 10 minutes, increases by 2 minutes per congestion level
                int baseTime = 10;
                int estimatedTime = baseTime + (congestionLevel * 2);

                JSONObject result = new JSONObject();
                result.put("firstIntersection", ctx.pathParam("firstId"));
                result.put("lastIntersection", ctx.pathParam("lastId"));
                result.put("congestionLevel", congestionLevel);
                result.put("estimatedTravelTimeMinutes", estimatedTime);

                ctx.json(result);
                ctx.status(200);
            } catch (IOException | InterruptedException e) {
                ctx.status(500).result("Service error: " + e.getMessage());
            }
        });


    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
