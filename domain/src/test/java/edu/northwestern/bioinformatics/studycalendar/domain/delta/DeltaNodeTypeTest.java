/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class DeltaNodeTypeTest extends DomainTestCase {
    private Reflections domainReflections;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainReflections = new Reflections("edu.northwestern.bioinformatics.studycalendar.domain");
    }

    public void testValueForDeltaClassWorksForKnownInstance() throws Exception {
        assertSame(DeltaNodeType.EPOCH, DeltaNodeType.valueForDeltaClass(EpochDelta.class));
    }

    public void testValueForDeltaClassWorksWithDynamicSubclass() throws Exception {
        Class<? extends Delta> subclass = new PlannedCalendarDelta() { }.getClass();
        assertSame(DeltaNodeType.PLANNED_CALENDAR, DeltaNodeType.valueForDeltaClass(subclass));
    }

    public void testThereIsOneDeltaNodeTypePerDeltaClass() throws Exception {
        List<String> errors = new ArrayList<String>();
        for (Class<? extends Delta> deltaClass : domainReflections.getSubTypesOf(Delta.class)) {
            if (Modifier.isAbstract(deltaClass.getModifiers()) || deltaClass.isAnonymousClass()) continue;
            try {
                DeltaNodeType.valueForDeltaClass(deltaClass);
            } catch (IllegalArgumentException iae) {
                errors.add(iae.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            fail("* " + StringUtils.join(errors.iterator(), "\n* "));
        }
    }

    public void testValueForNodeClassWorksForKnownInstance() throws Exception {
        assertSame(DeltaNodeType.POPULATION, DeltaNodeType.valueForNodeClass(Population.class));
    }

    public void testValueForNodeClassWorksWithDynamicSubclass() throws Exception {
        Class<? extends Changeable> subclass = new StudySegment() { }.getClass();
        assertSame(DeltaNodeType.STUDY_SEGMENT, DeltaNodeType.valueForNodeClass(subclass));
    }

    public void testThereIsOneDeltaNodeTypePerChangeableClass() throws Exception {
        List<String> errors = new ArrayList<String>();
        for (Class<? extends Changeable> cClass : domainReflections.getSubTypesOf(Changeable.class)) {
            if (Modifier.isAbstract(cClass.getModifiers()) || cClass.isAnonymousClass()) continue;
            try {
                DeltaNodeType.valueForNodeClass(cClass);
            } catch (IllegalArgumentException iae) {
                errors.add(iae.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            fail("* " + StringUtils.join(errors.iterator(), "\n* "));
        }
    }

    public void testDeltaInstanceUsesTheProvidedNode() throws Exception {
        PlannedActivityLabel label = new PlannedActivityLabel();
        assertSame(label, DeltaNodeType.PLANNED_ACTIVITY_LABEL.deltaInstance(label).getNode());
    }

    public void testDeltaInstanceWorksWithoutANode() throws Exception {
        assertTrue(DeltaNodeType.PERIOD.deltaInstance() instanceof PeriodDelta);
    }

    public void testNameIsHumanized() throws Exception {
        assertEquals("planned activity", DeltaNodeType.PLANNED_ACTIVITY.getNodeTypeName());
    }
}
