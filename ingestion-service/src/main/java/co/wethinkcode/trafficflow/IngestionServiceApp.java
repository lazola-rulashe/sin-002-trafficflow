package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IngestionServiceApp {

    public static JSONArray cleanedData;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        cleanedData = cleanedRecords();

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/records", ctx -> ctx.json(cleanedData));

    }

    public static JSONArray cleanedRecords() {
        Set<String> previousRows = new HashSet<>();

        StringBuilder result = new StringBuilder();
        result.append("Intersection_id,District,Signal_type,Active_flag\n");
        JSONArray json = new JSONArray();

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
                if (intersection_id == null || intersection_id.trim().isEmpty() || intersection_id.equals("N/A") || intersection_id.equals("unknown")) {
                    intersection_id = "TBD";
                }

                String district = toTitleCase(values[1].trim());
                if (district == null || district.trim().isEmpty() || district.equals("N/A") || district.equals("unknown")) {
                    district = "TBD";
                }

                String signal_type = toTitleCase(values[2].trim());
                if (signal_type == null || signal_type.trim().isEmpty() || signal_type.equals("N/A") || signal_type.equals("unknown")) {
                    signal_type = "TBD";
                }

                String active_flag = values[3].trim();
                if (active_flag.equals("N/A") || active_flag.equals("unknown")) {
                    active_flag = "TBD";
                }
                if (active_flag.toLowerCase(Locale.ROOT).equals("true") || active_flag.toLowerCase(Locale.ROOT).equals("yes") || active_flag.toLowerCase(Locale.ROOT).equals("1") || active_flag.toLowerCase(Locale.ROOT).equals("y")){
                    active_flag = "true";
                }
                if (active_flag.toLowerCase(Locale.ROOT).equals("false") || active_flag.toLowerCase(Locale.ROOT).equals("no") || active_flag.toLowerCase(Locale.ROOT).equals("0") || active_flag.toLowerCase(Locale.ROOT).equals("n")){
                    active_flag = "false";
                }

                String cleanedRow = intersection_id + "," + district + "," + signal_type + "," + active_flag;

                if (previousRows.contains(cleanedRow)) {
                    continue;
                }

                previousRows.add(cleanedRow);
                result.append(cleanedRow).append("\n");

                Map<String, String> record = new HashMap<>();
                record.put("Intersection_id", intersection_id);
                record.put("District", district);
                record.put("Signal_type", signal_type);
                record.put("Active_flag", active_flag);

                json.put(record);

            }

            } catch(IOException e){
                System.out.println("Could not read the file.");
            } catch(NullPointerException e){
                System.out.println("File not found on classpath");
            }


        return json;

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
