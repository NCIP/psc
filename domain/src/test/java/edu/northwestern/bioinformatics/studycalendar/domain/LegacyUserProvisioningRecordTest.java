/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

public class LegacyUserProvisioningRecordTest extends TestCase {
    public void testCsvFormat() throws Exception {
        LegacyUserProvisioningRecord r =
            new LegacyUserProvisioningRecord(
                "fflintstone",
                "Fred",
                "Flintstone",
                "1",
                "STUDY_COORDINATOR",
                "Bedrock",
                "Excavation"
            );

        String expected = "fflintstone,Fred,Flintstone,1,STUDY_COORDINATOR,Bedrock,Excavation";

        assertEquals("Wrong csv format", expected, r.csv());
    }

    public void testCsvFormatWithBlanks() throws Exception {
        LegacyUserProvisioningRecord r =
            new LegacyUserProvisioningRecord(
                "gslate",
                "George",
                "Slate",
                "0",
                "SITE_COORDINATOR",
                "Bedrock",
                ""
            );

        String expected = "gslate,George,Slate,0,SITE_COORDINATOR,Bedrock,";

        assertEquals("Wrong csv format", expected, r.csv());
    }

    public void testCsvFormatWithNulls() throws Exception {
    LegacyUserProvisioningRecord r =
        new LegacyUserProvisioningRecord(
            "gslate",
            "George",
            "Slate",
            "0",
            null,
            null,
            null
        );

    String expected = "gslate,George,Slate,0,,,";

    assertEquals("Wrong csv format", expected, r.csv());
}
}
