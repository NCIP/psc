package edu.northwestern.bioinformatics.studycalendar.domain;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

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
}
