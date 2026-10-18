package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.HashSet;
import java.util.Set;

public class IngestionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));


    }

    public static String cleanedRecords(){
        Set<String> previousRows = new HashSet<>();

        StringBuilder result = new StringBuilder();
        result.append("Intersection_id,District,Signal_type,Active_flag\n");


    }
}
