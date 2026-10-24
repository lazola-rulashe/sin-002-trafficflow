package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.CongestionPublisher;
import io.javalin.Javalin;
import org.json.JSONObject;

public class CongestionServiceApp {
    public static int currentLevel = 0;
    private static CongestionPublisher publisher;

    public static void main(String[] args) {
        int maxRetries = 5;
        int retryDelayMs = 2000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                publisher = new CongestionPublisher();
                System.out.println("MQ publisher initialized successfully");
                break;
            } catch (Exception e) {
                System.err.println("Failed to initialize MQ publisher (attempt " + (i + 1) + "/" + maxRetries + "): " + e.getMessage());
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

       app.get("/congestion", ctx ->{
           JSONObject response = new JSONObject();
           response.put("level", currentLevel);
           ctx.result(response.toString());
       });

       app.post("/congestion", ctx -> {
           int newLevel = Integer.parseInt(ctx.body());
           if(newLevel >=0 && newLevel <=8){
               currentLevel = newLevel;
               ctx.result("New level is : " + newLevel);
               ctx.status(200);

               if (publisher != null) {
                   try {
                       publisher.publishCongestionLevel(newLevel);
                   } catch (Exception e) {
                       System.err.println("Failed to publish congestion level: " + e.getMessage());
                   }
               }
           }else{
               ctx.result("This level is unknown");
               ctx.status(400);
           }
       });


    }
}
