package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IngestionServiceApp {

    public static List<Map<String, Object>> cleanedData;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        cleanedData = cleanedRecords();

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections", ctx -> ctx.json(cleanedData));

    }

    public static List<Map<String, Object>> cleanedRecords() {
        Set<String> previousRows = new HashSet<>();

        List<Map<String, Object>> array = new ArrayList<>();

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
                if (intersection_id.trim().isEmpty() || intersection_id.equals("N/A") || intersection_id.equals("unknown")) {
                    intersection_id = null;
                }

                String district = toTitleCase(values[1].trim());
                if (district.trim().isEmpty() || district.equals("N/A") || district.equals("unknown")) {
                    district = null;
                }

                String signal_type = toTitleCase(values[2].trim());
                if (signal_type.trim().isEmpty() || signal_type.equals("N/A") || signal_type.equals("unknown")) {
                    signal_type = null;
                }

                String active_flag = values[3].trim();
                if (active_flag.equals("N/A") || active_flag.equals("unknown")) {
                    active_flag = null;
                }
                boolean active = true;
                if (active_flag.toLowerCase(Locale.ROOT).equals("true") || active_flag.toLowerCase(Locale.ROOT).equals("yes") || active_flag.toLowerCase(Locale.ROOT).equals("1") || active_flag.toLowerCase(Locale.ROOT).equals("y")){
                    active = true;
                }
                if (active_flag.toLowerCase(Locale.ROOT).equals("false") || active_flag.toLowerCase(Locale.ROOT).equals("no") || active_flag.toLowerCase(Locale.ROOT).equals("0") || active_flag.toLowerCase(Locale.ROOT).equals("n")){
                    active = false;
                }

                String cleanedRow = intersection_id + "," + district + "," + signal_type + "," + active_flag;

                if (previousRows.contains(cleanedRow)) {
                    continue;
                }

                previousRows.add(cleanedRow);

                Map<String, Object> record = new HashMap<>();
                record.put("Intersection_id", intersection_id);
                record.put("District", district);
                record.put("Signal_type", signal_type);
                record.put("Active_flag", active);

                array.add(record);

            }

            } catch(IOException e){
                System.out.println("Could not read the file.");
            } catch(NullPointerException e){
                System.out.println("File not found on classpath");
            }


        return array;

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
