package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;

public class LegacyUserProvisioningRecordTest extends TestCase {
    public void testCsvFormat() throws Exception {
        LegacyUserProvisioningRecord r =
                new LegacyUserProvisioningRecord(
                    "fflintstone",
                    "Fred",
                    "Flintstone",
                    "Bedrock",
                    "Excavation",
                    "STUDY_COORDINATOR",
                    "1"
                );

        String expected = "fflintstone,Fred,Flintstone,Bedrock,Excavation,STUDY_COORDINATOR,1";

        assertEquals("Wrong csv format", expected, r.csv());
    }

    public void testCsvFormatWithBlanks() throws Exception {
        LegacyUserProvisioningRecord r =
        new LegacyUserProvisioningRecord(
            "gslate",
            "George",
            "Slate",
            "Bedrock",
            "",
            "SITE_COORDINATOR",
            "0"
        );

        String expected = "gslate,George,Slate,Bedrock,,SITE_COORDINATOR,0";

        assertEquals("Wrong csv format", expected, r.csv());
    }
}
