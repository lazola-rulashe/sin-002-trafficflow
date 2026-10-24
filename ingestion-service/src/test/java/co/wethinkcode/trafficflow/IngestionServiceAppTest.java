package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class IngestionServiceAppTest {

    @Test
    public void testToTitleCase() {
        assertEquals("Downtown", IngestionServiceApp.toTitleCase("downtown"));
        assertEquals("Downtown", IngestionServiceApp.toTitleCase("DOWNTOWN"));
        assertEquals("East Side", IngestionServiceApp.toTitleCase("east side"));
        assertEquals("", IngestionServiceApp.toTitleCase(""));
        assertNull(IngestionServiceApp.toTitleCase(null));
    }

    @Test
    public void testCleanedRecordsRemovesDuplicates() {
        List<Map<String, Object>> cleaned = IngestionServiceApp.cleanedRecords();

        boolean hasINT1005 = false;
        for (Map<String, Object> record : cleaned) {
            String id = (String) record.get("Intersection_id");
            if ("INT-1005".equals(id)) {
                if (hasINT1005) {
                    fail("Duplicate INT-1005 found in cleaned records");
                }
                hasINT1005 = true;
            }
        }
        assertTrue(hasINT1005, "INT-1005 should exist in cleaned records");
    }

    @Test
    public void testCleanedRecordsNormalizesCasing() {
        List<Map<String, Object>> cleaned = IngestionServiceApp.cleanedRecords();

        for (Map<String, Object> record : cleaned) {
            String district = (String) record.get("District");
            String signalType = (String) record.get("Signal_type");

            if (district != null) {
                assertEquals(district, IngestionServiceApp.toTitleCase(district),
                    "District should be title case");
            }
            if (signalType != null) {
                assertEquals(signalType, IngestionServiceApp.toTitleCase(signalType),
                    "Signal type should be title case");
            }
        }
    }

    @Test
    public void testCleanedRecordsHandlesMissingValues() {
        List<Map<String, Object>> cleaned = IngestionServiceApp.cleanedRecords();

        for (Map<String, Object> record : cleaned) {
            Object district = record.get("District");
            Object signalType = record.get("Signal_type");

            if (district == null) {
                assertNotNull(record.get("Intersection_id"),
                    "If district is null, intersection_id should not be null");
            }
            if (signalType == null) {
                assertNotNull(record.get("Intersection_id"),
                    "If signalType is null, intersection_id should not be null");
            }
        }
    }

    @Test
    public void testCleanedRecordsNormalizesBoolean() {
        List<Map<String, Object>> cleaned = IngestionServiceApp.cleanedRecords();

        for (Map<String, Object> record : cleaned) {
            Object active = record.get("Active_flag");
            assertNotNull(active, "Active_flag should not be null");
            assertTrue(active instanceof Boolean, "Active_flag should be a Boolean");
        }
    }
}
