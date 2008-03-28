package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_ASSIGNED_IDENTIFIER;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;
import org.dom4j.Element;

import java.util.Date;
import java.util.List;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study>{

    private StudyDao studyDao;

    public Element createElement(Study study) {
        Element elt = XsdElement.STUDY.create();
        STUDY_ASSIGNED_IDENTIFIER.addTo(elt, study.getAssignedIdentifier());

        Element eCalendar = getPlannedCalendarXmlSerializer(study).createElement(study.getPlannedCalendar());

        elt.add(eCalendar);

        for (Population population : study.getPopulations()) {
            Element ePopulation = getPopulationXmlSerializer(study).createElement(population);
            elt.add(ePopulation);
        }

        for (Amendment amendment : study.getAmendmentsList()) {
            Element eAmendment = getAmendmentSerializer(study).createElement(amendment);
            elt.add(eAmendment);
        }

        if (study.getDevelopmentAmendment() != null) {
            Amendment developmentAmendment = study.getDevelopmentAmendment();
            Element developmentAmendmentElement = getDevelopmentAmendmentSerializer(study).createElement(developmentAmendment);
            elt.add(developmentAmendmentElement);
        }

        return elt;
    }

    @SuppressWarnings({"unchecked"})
    public Study readElement(Element element) {
        validateElement(element);

        String key = XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(element);
        Study study = studyDao.getByAssignedIdentifier(key);
 
        if (study == null) {
            study = new Study();
            study.setAssignedIdentifier(key);


            Element eCalendar = element.element(PlannedCalendarXmlSerializer.PLANNED_CALENDAR);
            PlannedCalendar calendar = getPlannedCalendarXmlSerializer(study).readElement(eCalendar);
            study.setPlannedCalendar(calendar);

            PopulationXmlSerializer populationXmlSerializer = getPopulationXmlSerializer(study);
            for (Object oPopulation : element.elements(PopulationXmlSerializer.POPULATION)) {
                Element ePopulation = (Element) oPopulation;
                Population population = populationXmlSerializer.readElement(ePopulation);
                study.addPopulation(population);
            }
        }

        List<Element> eAmendments = element.elements(XsdElement.AMENDMENT.xmlName());

        Element currAmendment = findOriginalAmendment(eAmendments);
        while(currAmendment != null) {
            Amendment amendment = getAmendmentSerializer(study).readElement(currAmendment);
            if (!study.getAmendmentsList().contains(amendment)) {
                study.pushAmendment(amendment);
            }
            currAmendment = findNextAmendment(currAmendment, eAmendments);
        }

        Element developmentAmendmentElement = element.element(XsdElement.DEVELOPMENT_AMENDMENT.xmlName());
        if (developmentAmendmentElement != null) {
            Amendment developmentAmendment = getDevelopmentAmendmentSerializer(study).readElement(developmentAmendmentElement);
            study.setDevelopmentAmendment(developmentAmendment);
        }

        return study;
    }

    private void validateElement(Element element) {
        if (element.getName()!= null && (! element.getName().equals(STUDY.xmlName()))) {
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
        plannedCalendarXmlSerializer.setStudy(study);
        return plannedCalendarXmlSerializer;
    }

    protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {
        PopulationXmlSerializer populationXmlSerializer = (PopulationXmlSerializer) getBeanFactory().getBean("populationXmlSerializer");
        populationXmlSerializer.setStudy(study);
        return populationXmlSerializer;
    }

    protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {
        AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer ) getBeanFactory().getBean("amendmentXmlSerializer");
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

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
