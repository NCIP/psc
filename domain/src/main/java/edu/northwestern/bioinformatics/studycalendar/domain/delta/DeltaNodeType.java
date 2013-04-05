/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public enum DeltaNodeType {
    STUDY(Study.class, StudyDelta.class),
    POPULATION(Population.class, PopulationDelta.class),
    PLANNED_CALENDAR(PlannedCalendar.class, PlannedCalendarDelta.class),
    EPOCH(Epoch.class, EpochDelta.class),
    STUDY_SEGMENT(StudySegment.class, StudySegmentDelta.class),
    PERIOD(Period.class, PeriodDelta.class),
    PLANNED_ACTIVITY(PlannedActivity.class, PlannedActivityDelta.class),
    PLANNED_ACTIVITY_LABEL(PlannedActivityLabel.class, PlannedActivityLabelDelta.class)
    ;

    private Class<? extends Delta> deltaClass;
    private Class<? extends Changeable> nodeClass;

    <C extends Changeable> DeltaNodeType(Class<C> nodeClass, Class<? extends Delta<C>> deltaClass) {
        this.nodeClass = nodeClass;
        this.deltaClass = deltaClass;
    }

    ////// LOOKUPS

    public static DeltaNodeType valueForDeltaClass(Class<? extends Delta> deltaClass) {
        for (DeltaNodeType deltaNodeType : values()) {
            if (deltaNodeType.getDeltaClass().isAssignableFrom(deltaClass)) return deltaNodeType;
        }
        throw new IllegalArgumentException(String.format(
            "No node type defined for delta class %s", deltaClass.getName()));
    }

    public static DeltaNodeType valueForNodeClass(Class<? extends Changeable> nodeClass) {
        for (DeltaNodeType deltaNodeType : values()) {
            if (deltaNodeType.getNodeClass().isAssignableFrom(nodeClass)) return deltaNodeType;
        }
        throw new IllegalArgumentException(String.format(
            "No delta type defined for node class %s", nodeClass.getName()));
    }

    ///// INSTANTIATORS

    public Changeable nodeInstance() {
        try {
            return getNodeClass().newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarError("Could not instantiate %s", getNodeClass(), e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Could not instantiate %s", getNodeClass(), e);
        }
    }

    public Delta deltaInstance() {
        try {
            return getDeltaClass().newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarError("Could not instantiate %s", getDeltaClass(), e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Could not instantiate %s", getDeltaClass(), e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends Changeable> Delta<T> deltaInstance(T node) {
        try {
            Constructor<? extends Delta> constructor =
                getDeltaClass().getConstructor(getNodeClass());
            return constructor.newInstance(node);
        } catch (InstantiationException e) {
            throw new StudyCalendarError("Could not instantiate %s", getDeltaClass(), e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Could not instantiate %s", getDeltaClass(), e);
        } catch (NoSuchMethodException e) {
            throw new StudyCalendarError(
                "Could not locate appropriate constructor for %s", getDeltaClass(), e);
        } catch (InvocationTargetException e) {
            throw new StudyCalendarError("Could not instantiate %s", getDeltaClass(), e);
        }
    }

    ///// PROPERTIES

    public Class<? extends Delta> getDeltaClass() {
        return deltaClass;
    }

    public Class<? extends Changeable> getNodeClass() {
        return nodeClass;
    }

    public String getNodeTypeName() {
        return StringTools.humanizeClassName(getNodeClass().getSimpleName());
    }
}
