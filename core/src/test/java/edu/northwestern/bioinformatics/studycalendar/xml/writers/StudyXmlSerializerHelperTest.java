package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
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

    public void testResolvingActivityReferencesResolvesMultipleEquivalentActivities() throws Exception {
        Activity f;
        Source source = createSource("Some Things To Do", f = createActivity("Fly", "f"));

        Period p = createPeriod(1, 7, 4);
        p.addChild(Fixtures.createPlannedActivity(buildRef(f), 2));
        p.addChild(Fixtures.createPlannedActivity(buildRef(f), 6));

        Study in = Fixtures.createInDevelopmentTemplate("A");
        Add firstEpochAdd = (Add) in.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
        StudySegment aSegment = ((Epoch) firstEpochAdd.getChild()).getChildren().get(0);
        aSegment.addChild(p);

        helper.replaceActivityReferencesWithCorrespondingDefinitions(in, Arrays.asList(source));

        assertEquals("Did not resolve first",
            "Fly", p.getPlannedActivities().get(0).getActivity().getName());
        assertEquals("Did not resolve second",
            "Fly", p.getPlannedActivities().get(1).getActivity().getName());
    }

    private Activity buildRef(Activity a) {
        Activity ref = a.clone();
        ref.setName(null);
        ref.setDescription(null);
        ref.setType(null);
        ref.setSource(createSource(a.getSource().getName()));
        return ref;
    }
}
