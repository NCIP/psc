/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.TemplateTraversalHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StudyXmlSerializerHelper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ActivitySourceXmlSerializer activitySourceXmlSerializer;

    public Element generateSourcesElementWithActivities(Study study) {
        Collection<Activity> activities = findDistinctActivities(study);
        Collection<Source> sources = groupActivitiesBySource(activities);
        return activitySourceXmlSerializer.createElement(sources);
    }

    protected List<Activity> findAllActivities(Study study) {
        List<Activity> result = new LinkedList<Activity>();
        for (PlannedActivity pa : TemplateTraversalHelper.findAllNodes(study, PlannedActivity.class)) {
            result.add(pa.getActivity());
        }

        return result;
    }

    protected Set<Activity> findDistinctActivities(Study study) {
        return new HashSet<Activity>(findAllActivities(study));
    }

    protected Collection<Source> groupActivitiesBySource(Collection<Activity> all) {
        List<Source> result = new ArrayList<Source>();
        for (Activity a : all) {
            if (!result.contains(a.getSource())) {
                result.add(a.getSource().transientClone());
            }
            Source s = result.get(result.indexOf(a.getSource()));
            s.addActivity(a.transientClone());
        }
        return result;
    }

    public void replaceActivityReferencesWithCorrespondingDefinitions(Study study, Element eStudy) {
        Element eSource = eStudy.element("sources");
        if (eSource != null) {
            Collection<Source> sources = activitySourceXmlSerializer.readCollectionElement(eSource);

            replaceActivityReferencesWithCorrespondingDefinitions(study, sources);
        }
    }

    protected void replaceActivityReferencesWithCorrespondingDefinitions(Study study, Collection<Source> sources) {
        Collection<Activity> activityRefs = findAllActivities(study);
        log.debug("Resolving {} potential activity ref(s)", activityRefs.size());
        for (Activity ref : activityRefs) {
            log.debug("- attempting to resolve {}", ref);
            if (ref.getSource() == null) {
                throw new StudyCalendarValidationException(MessageFormat.format("Source is missing for activity reference [code={0}; source=(MISSING)]", ref.getCode()));
            }

            Source foundSource = ref.getSource().findSourceWhichHasSameName(sources);
            Activity foundActivityDef = ref.findActivityInCollectionWhichHasSameCode(foundSource.getActivities());

            if (foundActivityDef == null) {
                throw new StudyCalendarValidationException(MessageFormat.format("Problem resolving activity reference [code={0}; source={1}]", ref.getCode(), ref.getSource().getName()));
            }
            ref.updateActivity(foundActivityDef);
            ref.setProperties(new ArrayList<ActivityProperty>());
            for (ActivityProperty p : (new ArrayList<ActivityProperty>(foundActivityDef.getProperties()))) {
                ref.addProperty(p.clone());
            }
        }
    }

    public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer activitySourceXmlSerializer) {
        this.activitySourceXmlSerializer = activitySourceXmlSerializer;
    }
}
