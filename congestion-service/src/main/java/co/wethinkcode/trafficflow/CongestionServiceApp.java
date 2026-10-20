package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

public class CongestionServiceApp {
    public static int currentLevel = 0;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

       app.get("/congestion", ctx ->{

       });


    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
