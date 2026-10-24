package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.HeartbeatSubscriber;
import io.javalin.Javalin;
import org.json.JSONObject;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Timer;
import java.util.TimerTask;

public class IntersectionWatchdogApp {
    private static long lastHeartbeatTime = 0;
    private static boolean alertTriggered = false;
    private static final long HEARTBEAT_TIMEOUT_MS = 15000;

    public static void main(String[] args) {
        try {
            HeartbeatSubscriber subscriber = new HeartbeatSubscriber(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            JSONObject json = new JSONObject(textMessage.getText());
                            lastHeartbeatTime = System.currentTimeMillis();
                            alertTriggered = false;
                            System.out.println("Heartbeat received from intersection-service");
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to process heartbeat message: " + e.getMessage());
                    }
                }
            });

            Timer checkTimer = new Timer();
            checkTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    long timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeatTime;
                    if (timeSinceLastHeartbeat > HEARTBEAT_TIMEOUT_MS && !alertTriggered) {
                        alertTriggered = true;
                        System.err.println("ALERT: Intersection service heartbeat missed! Service may be down.");
                    }
                }
            }, 5000, 5000);
        } catch (Exception e) {
            System.err.println("Failed to initialize heartbeat subscriber: " + e.getMessage());
        }

        Javalin app = Javalin.create().start(7024);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert", ctx -> {
            org.json.JSONObject response = new org.json.JSONObject();
            response.put("alertTriggered", alertTriggered);
            response.put("lastHeartbeatTime", lastHeartbeatTime);
            response.put("intersectionServiceStatus", alertTriggered ? "DOWN" : "UP");
            ctx.result(response.toString());
        });
    }
}
