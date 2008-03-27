package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.AMENDMENT;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.DEVELOPMENT_AMENDMENT;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study>{
    // Elements
    public static final String STUDY = "study";

    // Attributes
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";

    private StudyDao studyDao;

    public Element createElement(Study study) {
        Element eStudy = element(STUDY)
                .addAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        Element eCalendar = getPlannedCalendarXmlSerializer(study).createElement(study.getPlannedCalendar());

        eStudy.add(eCalendar);

        for (Population population : study.getPopulations()) {
            Element ePopulation = getPopulationXmlSerializer(study).createElement(population);
            eStudy.add(ePopulation);
        }

        //TODO: make independent of order
        List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
        Collections.reverse(amendments);
        for (Amendment amendment : amendments) {
            Element eAmendment = getAmendmentSerializer(study).createElement(amendment);
            eStudy.add(eAmendment);
        }

        if (study.getDevelopmentAmendment() != null) {
            Amendment developmentAmendment = study.getDevelopmentAmendment();
            Element developmentAmendmentElement = getDevelopmentAmendmentSerializer(study).createElement(developmentAmendment);
            eStudy.add(developmentAmendmentElement);
        }

        return eStudy;
    }

    @SuppressWarnings({"unchecked"})
    public Study readElement(Element element) {
        validateElement(element);

        String key = element.attributeValue(ASSIGNED_IDENTIFIER);
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
        for (Element eAmendment : eAmendments) {
            Amendment amendment = getAmendmentSerializer(study).readElement(eAmendment);
            if (!study.getAmendmentsList().contains(amendment)) {
                study.pushAmendment(amendment);
            }
        }

        Element developmentAmendmentElement = element.element(XsdElement.DEVELOPMENT_AMENDMENT.xmlName());
        if (developmentAmendmentElement != null) {
            Amendment developmentAmendment = getDevelopmentAmendmentSerializer(study).readElement(developmentAmendmentElement);
            study.setDevelopmentAmendment(developmentAmendment);
        }

        return study;
    }

    private void validateElement(Element element) {
        if (element.getName()!= null && (! element.getName().equals(STUDY))) {
            throw new StudyCalendarValidationException("Element type is other than <study>");
        } else if (element.elements(AMENDMENT.xmlName()).isEmpty() && element.element(DEVELOPMENT_AMENDMENT.xmlName()) == null) {
            throw new StudyCalendarValidationException("<study> must have at minimum an <amendment> or <development-amendment> child");
        }
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
