package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.dom4j.Element;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study>{
    // Elements
    public static final String STUDY = "study";

    // Attributes
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";

    private StudyDao studyDao;
    private DeltaService deltaService;

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
        List<Amendment> amendments = new ArrayList(study.getAmendmentsList());
        Collections.reverse(amendments);
        for (Amendment amendment : amendments) {
            Element eAmendment = getAmendmentSerializer(study).createElement(amendment);
            eStudy.add(eAmendment);
        }

        return eStudy;
    }

    public Study readElement(Element element) {
        String key = element.attributeValue(ASSIGNED_IDENTIFIER);
        Study study = studyDao.getByAssignedIdentifier(key);
        if (study == null) {
            study = new Study();
            study.setAssignedIdentifier(key);

            PopulationXmlSerializer populationXmlSerializer = getPopulationXmlSerializer(study);
            for (Object oPopulation : element.elements(PopulationXmlSerializer.POPULATION)) {
                Element ePopulation = (Element) oPopulation;
                Population population = populationXmlSerializer.readElement(ePopulation);
                study.addPopulation(population);
            }

            Element eCalendar = element.element(PlannedCalendarXmlSerializer.PLANNED_CALENDAR);
            PlannedCalendar calendar = (PlannedCalendar) getPlannedCalendarXmlSerializer(study).readElement(eCalendar);
            study.setPlannedCalendar(calendar);

            List<Element> eAmendments = element.elements(AmendmentXmlSerializer.AMENDMENT);
            for (Element eAmendment : eAmendments) {
                Amendment amendment = getAmendmentSerializer(study).readElement(eAmendment);
                study.pushAmendment(amendment);
            }
        }
        return study;
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
        return amendmentSerializer;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
