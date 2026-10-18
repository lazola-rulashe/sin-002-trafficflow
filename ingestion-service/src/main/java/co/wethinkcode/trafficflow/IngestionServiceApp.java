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

            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 4) {
                    continue;
                }

                String intersection_id = values[0].trim().toUpperCase();

                String district = toTitleCase(values[1].trim());
                if (district == null || district.trim().isEmpty() || district.equals("N/A") || district.equals("unknown")) {
                    district = "TBD";
                }

                String signal_type = toTitleCase(values[2].trim());
                if (signal_type == null || signal_type.trim().isEmpty() || signal_type.equals("N/A") || signal_type.equals("unknown")) {
                    signal_type = "TBD";
                }




            } catch(IOException e){
                System.out.println("Could not read the file.");
            } catch(NullPointerException e){
                System.out.println("File not found on classpath");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toString();

    }
    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

}
