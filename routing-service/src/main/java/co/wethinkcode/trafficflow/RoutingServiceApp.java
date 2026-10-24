package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.CongestionSubscriber;
import io.javalin.Javalin;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
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
    public static int cachedCongestionLevel = 0;

    public static void main(String[] args) {
        try {
            CongestionSubscriber subscriber = new CongestionSubscriber(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            JSONObject json = new JSONObject(textMessage.getText());
                            cachedCongestionLevel = json.getInt("level");
                            System.out.println("Updated congestion level from MQ: " + cachedCongestionLevel);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to process congestion message: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to initialize MQ subscriber: " + e.getMessage());
        }

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
                    String intersectionId = (String) inter.get("id");
                    if (requestedId.equals(intersectionId)) {
                        ctx.json(inter);
                        ctx.status(200);
                        return;
                    }
                }

                ctx.result("Intersection unknown");
                ctx.status(404);

            }catch(Exception e){
                ctx.status(503).result("Intersection service unavailable");
            }

        });

        app.get("/congestion", ctx -> {
            JSONObject response = new JSONObject();
            response.put("level", cachedCongestionLevel);
            ctx.result(response.toString());
        });

        app.get("/route/{fistId}/{lastId}", ctx -> {

        });


    }
}
