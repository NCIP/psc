package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;

public class PopulationXmlSerializer extends AbstractStudyCalendarXmlDependentSerializer<Study, Population> {
    public static final String ABBREVIATION = "abbreviation";
    public static final String NAME = "name";
    public static final String POPULATION = "population";

    private PopulationDao populationDao;

    public Element createElement(Population child) {
        return element(POPULATION)
                .addAttribute(ABBREVIATION, child.getAbbreviation())
                .addAttribute(NAME, child.getName());
    }

    public Population readElement(Study study, Element element) {
        String abbreviation = element.attributeValue(ABBREVIATION);
        Population population = populationDao.getByAbbreviation(study, abbreviation);
        if (population == null) {
            population = new Population();
            population.setAbbreviation(abbreviation);
            population.setName(element.attributeValue(NAME));
            population.setStudy(study);
        }
        return population;
    }

    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }
}
