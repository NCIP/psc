package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {

    private StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer;
    private ActivitySourceXmlSerializer activitySourceXmlSerializer;
    private StudyXmlSerializerHelper studyXmlSerializerHelper;

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

        Element eSources = studyXmlSerializerHelper.generateSourcesElementWithActivities(study);
        elt.add(eSources);

        return elt;
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

        studyXmlSerializerHelper.replaceActivityReferencesWithCorrespondingDefinitions(study, element);

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

    @Required
    public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer serializer) {
        this.activitySourceXmlSerializer = serializer;
    }

    @Required
    public void setStudyXmlSerializerHelper(StudyXmlSerializerHelper studyXmlSerializerHelper) {
        this.studyXmlSerializerHelper = studyXmlSerializerHelper;
    }
}
