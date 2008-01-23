package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
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
            Element ePopulation = getPopulationSerializer(study).createElement(population);
            eStudy.add(ePopulation);
        }

        return eStudy;
    }

    public Study readElement(Element element) {
        String key = element.attributeValue(ASSIGNED_IDENTIFIER);
        Study study = studyDao.getByAssignedIdentifier(key);
        if (study == null) {
            study = new Study();
            study.setAssignedIdentifier(key);

            PopulationXmlSerializer populationXmlSerializer = getPopulationSerializer(study);
            for (Object oPopulation : element.elements(PopulationXmlSerializer.POPULATION)) {
                Element ePopulation = (Element) oPopulation;
                Population population = populationXmlSerializer.readElement(ePopulation);
                study.addPopulation(population);
            }

            Element eCalendar = element.element(PlannedCalendarXmlSerializer.PLANNED_CALENDAR);
            PlannedCalendar calendar = (PlannedCalendar) getPlannedCalendarXmlSerializer(study).readElement(eCalendar);
            study.setPlannedCalendar(calendar);
        }
        return study;
    }

    protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {
        return new PlannedCalendarXmlSerializer(study);
    }

    protected PopulationXmlSerializer getPopulationSerializer(Study study) {
        return new PopulationXmlSerializer(study);
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
