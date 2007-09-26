package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.util.StringEncrypter;

public class StringEncoderTest extends StudyCalendarTestCase {

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void testDecryption() throws Exception {
        StringEncrypter se = new StringEncrypter();
        String s = se.decrypt("yDq1c1nU4E7almNpMZNexg==");
        assertEquals("system_admin", s);
    }

     public void testEncryption() throws Exception {
        StringEncrypter se = new StringEncrypter();
        String encryptedText = se.encrypt("superuser");
        assertEquals("sAybp9IVe7feRIv4iO8hcA==", encryptedText);
        String decryptedText = se.decrypt(encryptedText);
        assertEquals("superuser", decryptedText);
        System.out.println("SYSTEM_ADMIN: "     + se.encrypt("system_admin"));
        System.out.println("ra_1: "            + se.encrypt("ra_1"));
        System.out.println("ra_2: "            + se.encrypt("ra_2"));
        System.out.println("study_admin1: "    + se.encrypt("study_admin1"));
        System.out.println("study_admin2: "    + se.encrypt("study_admin2"));
        System.out.println("studycd_1: "       + se.encrypt("studycd_1"));
        System.out.println("studycd_2: "       + se.encrypt("studycd_2"));
        System.out.println("participantcd_1: " + se.encrypt("participantcd_1"));
        System.out.println("participantcd_2: " + se.encrypt("participantcd_2"));
        System.out.println("sc_systemadmin: "  + se.encrypt("systemadmin"));
        System.out.println("sitecd_1: "        + se.encrypt("sitecd_1"));
        System.out.println("sitecd_2: "        + se.encrypt("sitecd_2"));

    }

}
