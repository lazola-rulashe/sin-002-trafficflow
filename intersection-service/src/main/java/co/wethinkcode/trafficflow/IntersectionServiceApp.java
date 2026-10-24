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

        app.get("/intersections", ctx -> {
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
                    Map<String, Object> record = jsonArray.getJSONObject(i).toMap();
                    Map<String, Object> normalizedRecord = new java.util.HashMap<>();
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals("Intersection_id")) {
                            normalizedRecord.put("id", entry.getValue());
                        } else if (key.equals("District")) {
                            normalizedRecord.put("district", entry.getValue());
                        } else if (key.equals("Signal_type")) {
                            normalizedRecord.put("signalType", entry.getValue());
                        } else if (key.equals("Active_flag")) {
                            normalizedRecord.put("active", entry.getValue());
                        } else {
                            normalizedRecord.put(key, entry.getValue());
                        }
                    }
                    intersections.add(normalizedRecord);
                }

                ctx.json(intersections);
            } catch (Exception e) {
                ctx.status(503).result("Ingestion service unavailable: " + e.getMessage());
            }
        });

        app.get("/intersections/{id}", ctx -> {
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
                    Map<String, Object> record = jsonArray.getJSONObject(i).toMap();
                    Map<String, Object> normalizedRecord = new java.util.HashMap<>();
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals("Intersection_id")) {
                            normalizedRecord.put("id", entry.getValue());
                        } else if (key.equals("District")) {
                            normalizedRecord.put("district", entry.getValue());
                        } else if (key.equals("Signal_type")) {
                            normalizedRecord.put("signalType", entry.getValue());
                        } else if (key.equals("Active_flag")) {
                            normalizedRecord.put("active", entry.getValue());
                        } else {
                            normalizedRecord.put(key, entry.getValue());
                        }
                    }
                    intersections.add(normalizedRecord);
                }

                String requestedId = ctx.pathParam("id");

                for (Map<String, Object> inter : intersections) {
                    String intersectionId = (String) inter.get("id");
                    if (requestedId.equals(intersectionId)){
                        ctx.json(inter);
                        ctx.status(200);
                        return;
                    }
                }

                ctx.result("Intersection unknown");
                ctx.status(404);

            }catch(Exception e){
                ctx.status(503).result("Ingestion service unavailable: " + e.getMessage());
            }

                });


                // TODO (Validates intersection/district names (source of truth).) // Add domain endpoints for intersection-service here.

}
    }

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
