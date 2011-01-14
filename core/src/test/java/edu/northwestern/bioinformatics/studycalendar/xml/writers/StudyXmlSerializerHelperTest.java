package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static java.util.Arrays.asList;

public class StudyXmlSerializerHelperTest extends StudyCalendarTestCase {
    private StudyXmlSerializerHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        helper = new StudyXmlSerializerHelper();
    }

    public void testGroupActivitiesBySource() throws Exception {
        Source nu = createNamedInstance("nu-activities", Source.class);
        Source na = createNamedInstance("na-activities", Source.class);

        Activity cbc = createActivity("cbc");
        Activity bbc = createActivity("bbc");
        Activity nbc = createActivity("nbc");

        cbc.setSource(nu);
        bbc.setSource(na);
        nbc.setSource(na);

        Collection<Source> actual = helper.groupActivitiesBySource(asList(cbc, bbc, nbc));

        assertEquals("Wrong size", 2, actual.size());
        assertContains(actual, nu);
        assertContains(actual, na);

        List<Source> sorted = new ArrayList<Source>(actual);
        Collections.sort(sorted, NamedComparator.INSTANCE);

        Source actualNa = sorted.get(0);
        Source actualNu = sorted.get(1);

        assertTrue("Should be transient", actualNa.isMemoryOnly());
        assertTrue("Should be transient", actualNu.isMemoryOnly());

        assertEquals("Wrong size", 1, actualNu.getActivities().size());
        assertContains(actualNu.getActivities(), cbc);

        assertEquals("Wrong size", 2, actualNa.getActivities().size());
        assertContains(actualNa.getActivities(), bbc);
        assertContains(actualNa.getActivities(), nbc);

        assertTrue("Should be transient", actualNu.getActivities().get(0).isMemoryOnly());
        assertTrue("Should be transient", actualNa.getActivities().get(0).isMemoryOnly());
        assertTrue("Should be transient", actualNa.getActivities().get(1).isMemoryOnly());
    }

}
