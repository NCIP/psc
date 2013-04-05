/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;

public class PopulationXmlSerializer extends AbstractStudyCalendarXmlSerializer<Population> {
    public static final String ABBREVIATION = "abbreviation";
    public static final String NAME = "name";
    public static final String POPULATION = "population";
    private static final String POPULATION_ABBREVIATION_IS_INVALID_MESSAGE = "Population's abbreviation '%s' is invalid. Please make sure abbreviation don't contain spaces and less then 5 characters long.";

    public Element createElement(Population child) {
        return element(POPULATION)
                .addAttribute(ABBREVIATION, child.getAbbreviation())
                .addAttribute(NAME, child.getName());
    }

    public Population readElement(Element element) {
        String abbreviation = element.attributeValue(ABBREVIATION);
        String name = element.attributeValue(NAME);
        validateElement(abbreviation);
        Population population = new Population();
        population.setAbbreviation(abbreviation);
        if (name != null) {
            population.setName(name);
        }
        return population;
    }

    private void validateElement(String abbreviation) {
        StringBuffer errorMessageBuffer = new StringBuffer("");

        if (abbreviation != null && (abbreviation.contains(" ") || abbreviation.length() > 5)) {
            String errorMessage = String.format(POPULATION_ABBREVIATION_IS_INVALID_MESSAGE, abbreviation);
            errorMessageBuffer.append(errorMessage);
        }

        if (!StringUtils.isEmpty(errorMessageBuffer.toString())) {
            log.error(errorMessageBuffer.toString());

            StudyImportException studyImportException = new StudyImportException(errorMessageBuffer.toString());

            throw studyImportException;
        }
    }

}
