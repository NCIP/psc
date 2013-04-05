/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import gov.nih.nci.security.util.StringEncrypter;
import junit.framework.TestCase;

/**
 * Exploratory test for CSM's string encryption class.
 */
public class CsmStringEncoderTest extends TestCase {
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
        System.out.println("subjectcd_1: " + se.encrypt("subjectcd_1"));
        System.out.println("subjectcd_2: " + se.encrypt("subjectcd_2"));
        System.out.println("sc_systemadmin: "  + se.encrypt("systemadmin"));
        System.out.println("sitecd_1: "        + se.encrypt("sitecd_1"));
        System.out.println("sitecd_2: "        + se.encrypt("sitecd_2"));
        System.out.println("system_admin1: "   + se.encrypt("system_admin1"));
        System.out.println("system_admin2: "   + se.encrypt("system_admin2"));
    }
}
