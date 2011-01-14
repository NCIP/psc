package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.LONG_TITLE;

/**
 * @author Rhett Sutphin
 */
public class StudySnapshotXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
    private StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer;
    private ActivitySourceXmlSerializer activitySourceXmlSerializer;
    private StudyXmlSerializerHelper studyXmlSerializerHelper;

    @Override
    public Element createElement(Study study) {
        Element elt = XsdElement.STUDY_SNAPSHOT.create();
        XsdAttribute.STUDY_SNAPSHOT_ASSIGNED_IDENTIFIER.addTo(elt, study.getAssignedIdentifier());
        if (study.getProvider() !=null) {
            XsdAttribute.STUDY_PROVIDER.addTo(elt, study.getProvider());
        }
        if (study.getLongTitle() != null && study.getLongTitle().length() >0) {
            Element eltLongTitle = XsdElement.LONG_TITLE.create();
            eltLongTitle.addText(study.getLongTitle());
            elt.add(eltLongTitle);
        }
        for (StudySecondaryIdentifier studySecondaryIdent : study.getSecondaryIdentifiers()) {
            elt.add(studySecondaryIdentifierXmlSerializer.createElement(studySecondaryIdent));
        }

        Set<Population> pops = study.getPopulations();
        PopulationXmlSerializer populationXmlSerializer = createPopulationXmlSerializer(study);
        for (Population pop : pops) {
            elt.add(populationXmlSerializer.createElement(pop));
        }

        elt.add(createPlannedCalendarSerializer(study).createElement(study.getPlannedCalendar()));

        Element eSources = studyXmlSerializerHelper.generateSourcesElementWithActivities(study);
        elt.add(eSources);

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

        String provider =  XsdAttribute.STUDY_PROVIDER.from(element);
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

        for (Element identifierElt:(List<Element>) element.elements("secondary-identifier")) {
            study.addSecondaryIdentifier(studySecondaryIdentifierXmlSerializer.readElement(identifierElt));
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

        studyXmlSerializerHelper.replaceActivityReferencesWithCorrespondingDefinitions(study, element);

        return study;
    }

    private PlannedCalendarXmlSerializer createPlannedCalendarSerializer(Study parent) {
        PlannedCalendarXmlSerializer serializer
            = (PlannedCalendarXmlSerializer) getBeanFactory().getBean("plannedCalendarXmlSerializer");
        serializer.setSerializeEpoch(true);
        return serializer;
    }

    private PopulationXmlSerializer createPopulationXmlSerializer(Study parent) {
        PopulationXmlSerializer serializer
            = (PopulationXmlSerializer) getBeanFactory().getBean("populationXmlSerializer");
        return serializer;
    }
    
    @Required
    public void setStudySecondaryIdentifierXmlSerializer(StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer) {
        this.studySecondaryIdentifierXmlSerializer = studySecondaryIdentifierXmlSerializer;
    }

    @Required
    public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer activitySourceXmlSerializer) {
        this.activitySourceXmlSerializer = activitySourceXmlSerializer;
    }

    @Required
    public void setStudyXmlSerializerHelper(StudyXmlSerializerHelper studyXmlSerializerHelper) {
        this.studyXmlSerializerHelper = studyXmlSerializerHelper;
    }
}
