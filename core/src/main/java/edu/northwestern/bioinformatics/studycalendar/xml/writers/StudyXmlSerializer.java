package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.TemplateTraversalHelper;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {

    private StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer;
    private ActivitySourceXmlSerializer activitySourceXmlSerializer;

    public Element createElement(Study study) {
        Element elt = XsdElement.STUDY.create();
        STUDY_ASSIGNED_IDENTIFIER.addTo(elt, study.getAssignedIdentifier());
        LAST_MODIFIED_DATE.addToDateTime(elt, study.getLastModifiedDate());

        if (study.getProvider() != null) {
            STUDY_PROVIDER.addTo(elt, study.getProvider());
        }
        if (study.getLongTitle() != null && study.getLongTitle().length() >0) {
            Element eltLongTitle = XsdElement.LONG_TITLE.create();
            eltLongTitle.addText(study.getLongTitle());
            elt.add(eltLongTitle);
        }

        for (StudySecondaryIdentifier studySecondaryIdent : study.getSecondaryIdentifiers()) {
            elt.add(studySecondaryIdentifierXmlSerializer.createElement(studySecondaryIdent));
        }

        Element eCalendar = getPlannedCalendarXmlSerializer(study).createElement(study.getPlannedCalendar());

        elt.add(eCalendar);

        for (Amendment amendment : study.getAmendmentsList()) {
            Element eAmendment = getAmendmentSerializer(study).createElement(amendment);
            elt.add(eAmendment);
        }

        if (study.getDevelopmentAmendment() != null) {
            Amendment developmentAmendment = study.getDevelopmentAmendment();
            Element developmentAmendmentElement = getDevelopmentAmendmentSerializer(study).createElement(developmentAmendment);
            elt.add(developmentAmendmentElement);
        }

        Collection<Activity> activities = findAllActivities(study);
        Collection<Source> sources = groupActivitiesBySource(activities);
        Element sourceElement = activitySourceXmlSerializer.createElement(sources);
        elt.add(sourceElement);

        return elt;
    }

    private Collection<Activity> findAllActivities(Study study) {
        Collection<Activity> result = new HashSet<Activity>();
        for (Parent p : TemplateTraversalHelper.findRootParentNodes(study)) {
            for (PlannedActivity a : TemplateTraversalHelper.getInstance().findChildren(p, PlannedActivity.class)) {
                result.add(a.getActivity());
            }
        }
        return result;
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

    @SuppressWarnings({"unchecked"})
    public Study readElement(Element element){
        throw new UnsupportedOperationException("Need to pass the base study");
    }


    @SuppressWarnings({"unchecked"})
    public Study readElement(Element element, Study study) {
        validateElement(element);

        String key = XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(element);

        study.setAssignedIdentifier(key);
        String provider =  STUDY_PROVIDER.from(element);
        if (provider != null && provider.length() > 0) {
            study.setProvider(provider);
        }

        Element eltLongTitle = element.element(LONG_TITLE.xmlName());
        if (eltLongTitle != null) {
            String longTitleName = eltLongTitle.getText();
            if (longTitleName != null && longTitleName.length() > 0) {
                study.setLongTitle(longTitleName.replaceAll("\\s+"," ").trim());
            }
        }

        for(Element identifierElt:(List<Element>) element.elements("secondary-identifier")) {
            study.addSecondaryIdentifier(studySecondaryIdentifierXmlSerializer.readElement(identifierElt));
        }

        Element eCalendar = element.element(PlannedCalendarXmlSerializer.PLANNED_CALENDAR);
        PlannedCalendar calendar = getPlannedCalendarXmlSerializer(study).readElement(eCalendar);
        study.setPlannedCalendar(calendar);

        List<Element> eAmendments = element.elements(XsdElement.AMENDMENT.xmlName());

        Element currAmendment = findOriginalAmendment(eAmendments);
        while (currAmendment != null) {
            Amendment amendment = getAmendmentSerializer(study).readElement(currAmendment);
            study.pushAmendment(amendment);
            currAmendment = findNextAmendment(currAmendment, eAmendments);
        }

        Element developmentAmendmentElement = element.element(XsdElement.DEVELOPMENT_AMENDMENT.xmlName());
        if (developmentAmendmentElement != null) {
            Amendment developmentAmendment = getDevelopmentAmendmentSerializer(study).readElement(developmentAmendmentElement);
            study.setDevelopmentAmendment(developmentAmendment);
        }

        Element eSource = element.element("sources");
        if (eSource != null) {
            Collection<Source> sources = activitySourceXmlSerializer.readCollectionElement(eSource);

            Collection<Activity> activityRefs = findAllActivities(study);
            for (Activity ref : activityRefs) {
                if (ref.getSource() == null) {
                    throw new StudyCalendarValidationException(MessageFormat.format("Source is missing for activity reference [code={0}; source=(MISSING)]", ref.getCode()));
                }

                Source foundSource = ref.getSource().findSourceWhichHasSameName(sources);
                Activity foundActivityDef = ref.findActivityInCollectionWhichHasSameCode(foundSource.getActivities());

                if (foundActivityDef == null) {
                    throw new StudyCalendarValidationException(MessageFormat.format("Problem resolving activity reference [code={0}; source={1}]", ref.getCode(), ref.getSource().getName()));
                }
                ref.updateActivity(foundActivityDef);
                ref.getProperties().clear();
                for (ActivityProperty p : (new ArrayList<ActivityProperty>(foundActivityDef.getProperties()))) {
                    ref.addProperty(p.clone());
                }
            }
        }

        return study;
    }

    private void validateElement(Element element) {
        if (element.getName() != null && (!element.getName().equals(STUDY.xmlName()))) {
            throw new StudyCalendarValidationException("Element type is other than <study>");
        } else if (element.elements(AMENDMENT.xmlName()).isEmpty() && element.element(DEVELOPMENT_AMENDMENT.xmlName()) == null) {
            throw new StudyCalendarValidationException("<study> must have at minimum an <amendment> or <development-amendment> child");
        }
    }

    private Element findNextAmendment(Element currAmendment, List<Element> eAmendments) {
        String name = XsdAttribute.AMENDMENT_NAME.from(currAmendment);
        Date date = XsdAttribute.AMENDMENT_DATE.fromDate(currAmendment);
        String key = new Amendment.Key(date, name).toString();

        for (Element amend : eAmendments) {
            if (key.equals(XsdAttribute.AMENDMENT_PREVIOUS_AMENDMENT_KEY.from(amend))) return amend;
        }
        return null;
    }

    private Element findOriginalAmendment(List<Element> eAmendments) {
        for (Element amend : eAmendments) {
            if (XsdAttribute.AMENDMENT_PREVIOUS_AMENDMENT_KEY.from(amend) == null) return amend;
        }
        return null;
    }

    public String readAssignedIdentifier(InputStream in) {
        Document doc = deserializeDocument(in);
        Element elt = doc.getRootElement();
        validateElement(elt);
        return XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(elt);
    }

    protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {
        PlannedCalendarXmlSerializer plannedCalendarXmlSerializer = (PlannedCalendarXmlSerializer) getBeanFactory().getBean("plannedCalendarXmlSerializer");
        return plannedCalendarXmlSerializer;
    }

    protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {
        PopulationXmlSerializer populationXmlSerializer = (PopulationXmlSerializer) getBeanFactory().getBean("populationXmlSerializer");
        return populationXmlSerializer;
    }

    protected StudyXmlSerializer getStudyXmlSerializer(Study study) {
        StudyXmlSerializer studyXmlSerializer = (StudyXmlSerializer) getBeanFactory().getBean("studyXmlSerializer");
        return studyXmlSerializer;
    }

    protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {
        AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
        amendmentSerializer.setStudy(study);
        amendmentSerializer.setDevelopmentAmendment(false);
        return amendmentSerializer;
    }

    protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
        AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
        amendmentSerializer.setStudy(study);
        amendmentSerializer.setDevelopmentAmendment(true);
        return amendmentSerializer;
    }

    @Required
    public void setStudySecondaryIdentifierXmlSerializer(StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer) {
        this.studySecondaryIdentifierXmlSerializer = studySecondaryIdentifierXmlSerializer;
    }

    public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer serializer) {
        this.activitySourceXmlSerializer = serializer;
    }
}
