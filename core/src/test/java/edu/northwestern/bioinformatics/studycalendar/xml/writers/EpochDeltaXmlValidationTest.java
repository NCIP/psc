/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Saurabh Agrawal
 */
public class EpochDeltaXmlValidationTest extends AbstractXmlValidationTestCase {

    private Element eDelta;

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        epochDelta = new EpochDelta(epoch1);
        epochDelta.setGridId("6b2d06a3-f521-4ef8-9cae-3eb73f6f6bf4");
        epochDelta.addChange(add1);


        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

    }

    public void testValidteElement() {
        assertFalse(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));
        assertEquals(String.format("\n released amendment present in the system does have  any delta matching with provied grid id %s and node id  %s of delta.\n",
                epochDelta.getGridId(), epoch1.getGridId()), epochDeltaXmlSerializer.validate(amendment, eDelta).toString());

        amendment.addDelta(epochDelta);
        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));

    }


    public void testValidateElementForDifferentNumberOfChanges() {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        assertEquals(String.format("Imported document has different number of Changes for  delta (id :%s).  Please make sure changes are identical and they are in same order.", epochDelta.getGridId())
                , epochDeltaXmlSerializer.validate(amendment, eDelta).toString());


    }

    public void testValidateForInValidAddChangeContent() throws IOException, SAXException {
        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setActivity(activity3);

        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("activity references are different for PlannedActivity"));


    }

    public void testValidateForInValidPropertyChangeAttributes() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        add1.setIndex(5);


        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("index is different. expected:5 , found (in imported document) :0"));


    }

    public void testValidateForIdenticalDelta() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));


    }
}
