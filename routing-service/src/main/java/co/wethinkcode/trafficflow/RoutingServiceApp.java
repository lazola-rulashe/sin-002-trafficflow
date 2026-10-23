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
    public static String firstInterId;
    public static String lastInterId;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections/{id}", ctx -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7021/intersections"))
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

                String requestedId = ctx.pathParam("id");


                for (Map<String, Object> inter : intersections) {
                    for (Map.Entry<String, Object> entry : inter.entrySet()) {
                        String intersectionId1 = (String) inter.get("Intersection_id");
                        if (requestedId.equals(intersectionId1)) {
                            firstInterId = intersectionId1;
                            ctx.status(200);
                            return;
                        }
                        ctx.result("Intersection unknown");
                        ctx.status(404);
                    }
                }

            }catch(Exception e){
                ctx.status(404).result("Intersection ID unknown");
            }

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

        app.get("/route/{fistId}/{lastId}", ctx -> {

        });


    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
