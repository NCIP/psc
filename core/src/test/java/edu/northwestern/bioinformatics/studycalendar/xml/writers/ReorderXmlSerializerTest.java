/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class ReorderXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ReorderXmlSerializer xmlSerializer;
    private Epoch epoch1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlSerializer = new ReorderXmlSerializer(); 
        createReorder();
    }

    private Reorder createReorder() {
        epoch1 = Fixtures.createNamedInstance("Treatment", Epoch.class);
        epoch1.setGridId("690361c1-433e-4a25-bfe2-09db0ce2edab");

        Reorder reorder = Reorder.create(epoch1, 2, 1);
        reorder.setGridId("2c845525-46cb-4c05-a0d8-9de0e866b61c");
        return reorder;
    }

    public void testValidateElement() throws Exception {
        Reorder reorder = createReorder();
        Element actual = xmlSerializer.createElement(reorder);
        assertTrue(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));
        reorder.setOldIndex(null);
        assertFalse(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));

        reorder = createReorder();
        assertTrue(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));
        reorder.setNewIndex(null);
        assertFalse(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));

        reorder = createReorder();
        assertTrue(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));
        reorder.setChild(null);
        assertFalse(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));

        reorder = createReorder();
        assertTrue(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));
        reorder.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(xmlSerializer.validateElement(reorder, actual).toString()));


        assertFalse(StringUtils.isBlank(xmlSerializer.validateElement(null, actual).toString()));
    }
}
