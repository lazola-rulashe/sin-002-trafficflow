package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.HeartbeatPublisher;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class IntersectionServiceApp {
    public static List<Map<String, Object>> intersections;
    private static HeartbeatPublisher heartbeatPublisher;


    public static void main(String[] args) {
        try {
            heartbeatPublisher = new HeartbeatPublisher();
            Timer heartbeatTimer = new Timer();
            heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        heartbeatPublisher.publishHeartbeat();
                        System.out.println("Heartbeat sent");
                    } catch (Exception e) {
                        System.err.println("Failed to send heartbeat: " + e.getMessage());
                    }
                }
            }, 0, 5000);
        } catch (Exception e) {
            System.err.println("Failed to initialize heartbeat publisher: " + e.getMessage());
        }

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersectios", ctx -> {
            ctx.json(intersections);
        });

        app.get("/intersections/{Intersection_id}", ctx -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7020/intersections"))
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

                String requestedId = ctx.pathParam("Intersection_id");


                for (Map<String, Object> inter : intersections) {
                    for (Map.Entry<String, Object> entry : inter.entrySet()) {
                        String intersectionId = (String) inter.get("Intersection_id");
                        if (requestedId.equals(intersectionId)){
                            ctx.json(inter);
                            ctx.status(200);
                            return;
                        }
                        ctx.result("Intersection unknown");
                        ctx.status(404);

                    }
                }

            }catch(Exception e){
                ctx.status(503).result("Ingestion service unavailable " + e.getMessage());
            }

                });


                // TODO (Validates intersection/district names (source of truth).) // Add domain endpoints for intersection-service here.

}
    }

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
