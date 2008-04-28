package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class StudySnapshotXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
    @Override
    public Element createElement(Study study) {
        Element elt = XsdElement.STUDY_SNAPSHOT.create();
        XsdAttribute.STUDY_SNAPSHOT_ASSIGNED_IDENTIFIER.addTo(elt, study.getAssignedIdentifier());

        Set<Population> pops = study.getPopulations();
        PopulationXmlSerializer populationXmlSerializer = createPopulationXmlSerializer(study);
        for (Population pop : pops) {
            elt.add(populationXmlSerializer.createElement(pop));
        }

        elt.add(createPlannedCalendarSerializer(study).createElement(study.getPlannedCalendar()));

        return elt;
    }

    @Override
    public Study readElement(Element element) {
        Study study = new Study();
        study.setAssignedIdentifier(
            XsdAttribute.STUDY_SNAPSHOT_ASSIGNED_IDENTIFIER.from(element));
        if (study.getAssignedIdentifier() == null) {
            throw new StudyCalendarValidationException(
                XsdAttribute.STUDY_SNAPSHOT_ASSIGNED_IDENTIFIER.xmlName() + " is required for "
                    + XsdElement.STUDY_SNAPSHOT.xmlName());
        }

        List<Element> popElts = XsdElement.POPULATION.allFrom(element);
        PopulationXmlSerializer populationXmlSerializer = createPopulationXmlSerializer(study);
        for (Element popElt : popElts) {
            study.addPopulation(populationXmlSerializer.readElement(popElt));
        }

        Element pcElt = XsdElement.PLANNED_CALENDAR.from(element);
        if (pcElt == null) {
            study.setPlannedCalendar(new PlannedCalendar());
        } else {
            study.setPlannedCalendar(
                (PlannedCalendar) createPlannedCalendarSerializer(study).readElement(pcElt));
        }

        return study;
    }

    private PlannedCalendarXmlSerializer createPlannedCalendarSerializer(Study parent) {
        PlannedCalendarXmlSerializer serializer
            = (PlannedCalendarXmlSerializer) getBeanFactory().getBean("plannedCalendarXmlSerializer");
        serializer.setSerializeEpoch(true);
        serializer.setStudy(parent);
        return serializer;
    }

    private PopulationXmlSerializer createPopulationXmlSerializer(Study parent) {
        PopulationXmlSerializer serializer
            = (PopulationXmlSerializer) getBeanFactory().getBean("populationXmlSerializer");
        serializer.setStudy(parent);
        return serializer;
    }
}
