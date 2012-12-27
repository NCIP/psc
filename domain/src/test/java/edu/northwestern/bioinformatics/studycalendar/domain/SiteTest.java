/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class SiteTest extends TestCase {
    private Site site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        site = new Site();
    }

    public void testGetStudySiteForUnaffiliatedStudy() throws Exception {
        Study s2 = Fixtures.createSingleEpochStudy("S2", "E");
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S3", "E"), site));
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S4", "E"), site));

        assertNull(site.getStudySite(s2));
    }

    public void testAssignedIdentifierDefaultsToName() throws Exception {
        site.setName("arb");
        site.setAssignedIdentifier(null);
        assertEquals("arb", site.getAssignedIdentifier());
    }

    public void testExplicitAssignedIdentifierTrumps() throws Exception {
        site.setName("foo");
        site.setAssignedIdentifier("bar");
        assertEquals("bar", site.getAssignedIdentifier());
    }
    
    public void testGetStudySiteForAffiliatedStudy() throws Exception {
        Study s3 = Fixtures.createSingleEpochStudy("S3", "E");
        site.addStudySite(Fixtures.createStudySite(s3, site));
        site.addStudySite(Fixtures.createStudySite(Fixtures.createSingleEpochStudy("S4", "E"), site));

        assertNotNull(site.getStudySite(s3));
        assertSame(s3, site.getStudySite(s3).getStudy());
        assertSame(site, site.getStudySite(s3).getSite());
    }
    
    public void testAddManagedStudyAddsTheStudy() throws Exception {
        Study s = new Study();
        site.addManagedStudy(s);
        assertTrue(site.getManagedStudies().contains(s));
    }

    public void testAddManagedStudyMaintainsTheBidirectionalRelationship() throws Exception {
        Study s = new Study();
        site.addManagedStudy(s);
        assertTrue(s.getManagingSites().contains(site));
    }

    public void testRemoveManagedStudyRemovesTheStudy() throws Exception {
        Study s = new Study();
        site.addManagedStudy(s);
        site.removeManagedStudy(s);
        assertFalse(site.getManagedStudies().contains(s));
    }

    public void testAddManagedStudyRemovesTheBidirectionalRelationship() throws Exception {
        Study s = new Study();
        site.addManagedStudy(s);
        site.removeManagedStudy(s);
        assertFalse(s.getManagingSites().contains(site));
    }

    public void testToString() throws Exception {
        site.setId(49);
        site.setName("An Area");
        site.setAssignedIdentifier("51");
        assertEquals("Wrong string rep",
            "Site[id=49; name=An Area; assignedIdentifier=51]",
            site.toString());
    }

    public void testIsNameEditableWithoutProvider() {
        assertTrue("Should be editable", site.isNameEditable());
    }

    public void testIsNameEditableWithProvider() {
        site.setProvider("orb");
        assertFalse("Should not be editable", site.isNameEditable());
    }

    public void testIsAssignedIdentifierEditableWithoutProvider() {
        assertTrue("Should be editable", site.isAssignedIdentifierEditable());
    }   

    public void testIsAssignedIdentifierEditableWithProvider() {
        site.setProvider("orb");
        assertFalse("Should not be editable", site.isAssignedIdentifierEditable());
    }
}
