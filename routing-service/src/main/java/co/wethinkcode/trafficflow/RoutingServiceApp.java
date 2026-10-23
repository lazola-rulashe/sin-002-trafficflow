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

        });


    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
