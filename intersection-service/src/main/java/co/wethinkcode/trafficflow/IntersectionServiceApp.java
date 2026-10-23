package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntersectionServiceApp {
    public static List<Map<String, Object>> intersections;


    public static void main(String[] args) throws IOException, InterruptedException {
        Javalin app = Javalin.create().start(7021);

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

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections", ctx -> {
            ctx.json(intersections);
        });

        app.get("/intersections/{Intersection_id}", ctx -> {
                    try {

                        String requestedId = ctx.pathParam("Intersection_id");

                        for (Map<String, Object> inter : intersections) {
                            String intersectionId = (String) inter.get("Intersection_id");
                            if (requestedId.equals(intersectionId)) {
                                ctx.json(inter);
                                ctx.status(200);
                                return;
                            }
                        }


                    } catch (Exception e) {
                        ctx.status(404).result("Intersection not found " + e.getMessage());
                    }

                });

            app.get("/district/{District}", ctx -> {
                try {

                    String requestedName = ctx.pathParam("District");

                    for (Map<String, Object> inter : intersections) {
                        String districtName = (String) inter.get("District");
                        if (requestedName.equals(districtName)) {
                            ctx.json(inter);
                            ctx.status(200);
                            return;
                        }
                    }


                }catch(Exception e){
                    ctx.status(404).result("District name not found " + e.getMessage());
                }

                });


                // TODO (Validates intersection/district names (source of truth).) // Add domain endpoints for intersection-service here.

}
    }

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
