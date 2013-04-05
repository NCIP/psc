/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNegative;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class StudySecondaryIdentifierTest extends DomainTestCase {
    public void testNaturalOrderIsByTypeFirst() throws Exception {
        StudySecondaryIdentifier id0 = createStudyIdentifier("A", "1");
        StudySecondaryIdentifier id1 = createStudyIdentifier("B", "0");
        assertNegative(id0.compareTo(id1));
        assertPositive(id1.compareTo(id0));
    }

    public void testNaturalOrderIsByValueSecond() throws Exception {
        StudySecondaryIdentifier id0 = createStudyIdentifier("A", "1");
        StudySecondaryIdentifier id1 = createStudyIdentifier("A", "0");
        assertNegative(id1.compareTo(id0));
        assertPositive(id0.compareTo(id1));
    }
    
    public void testNaturalOrderIsByStudyThird() throws Exception {
        Study s0 = setId(0, createReleasedTemplate());
        Study s1 = setId(1, createReleasedTemplate());
        StudySecondaryIdentifier id0 = addSecondaryIdentifier(s0, "A", "0");
        StudySecondaryIdentifier id1 = addSecondaryIdentifier(s1, "A", "0");

        assertNegative(id0.compareTo(id1));
        assertPositive(id1.compareTo(id0));
    }

    public void testIsCloneable() throws Exception {
        StudySecondaryIdentifier ident = createStudyIdentifier("A", "1");
        StudySecondaryIdentifier clone = ident.clone();
        assertNotSame(ident, clone);
    }

    public void testCloningRemovesStudyRef() throws Exception {
        Study s = createReleasedTemplate();
        addSecondaryIdentifier(s, "B", "7");
        StudySecondaryIdentifier clone = s.getSecondaryIdentifiers().first().clone();
        assertNull("Parent ref not removed", clone.getStudy());
    }

    public void testEquals() {
        StudySecondaryIdentifier identA1 = createStudyIdentifier("A", "1");
        StudySecondaryIdentifier identA1Too = createStudyIdentifier("A", "1");
        assertEquals("Identifiers are not equal", identA1,  identA1Too);

    }

    public void testSetEquals() {
        Set<StudySecondaryIdentifier> identsA = new HashSet<StudySecondaryIdentifier>();
        Set<StudySecondaryIdentifier> identsB = new HashSet<StudySecondaryIdentifier>();

        StudySecondaryIdentifier identA1 = createStudyIdentifier("A", "1");
        identsA.add(identA1);

        StudySecondaryIdentifier identA1Too = createStudyIdentifier("A", "1");
        identsB.add(identA1Too);
        
        assertEquals("Identifiers are not equal", identsA,  identsB);

    }
}
