package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class IngestionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));


    }

    public static String cleanedRecords() {
        Set<String> previousRows = new HashSet<>();

        StringBuilder result = new StringBuilder();
        result.append("Intersection_id,District,Signal_type,Active_flag\n");

        try (InputStream input = IngestionServiceApp.class
                .getResourceAsStream("/intersections-legacy.csv");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {

            reader.readLine();


        } catch (IOException e) {
            System.out.println("Could not read the file.");
        } catch (NullPointerException e) {
            System.out.println("File not found on classpath");
        }

        return result.toString();
    }

}
