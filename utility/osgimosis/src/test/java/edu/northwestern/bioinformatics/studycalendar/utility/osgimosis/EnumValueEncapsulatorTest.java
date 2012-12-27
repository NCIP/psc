/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Size;

/**
 * @author Rhett Sutphin
 */
public class EnumValueEncapsulatorTest extends OsgimosisTestCase {
    private EnumValueEncapsulator enumTranslator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enumTranslator = new EnumValueEncapsulator(Thread.currentThread().getContextClassLoader());
    }

    public void testTranslatesEnumInstancesIntoTheLocalVersion() throws Exception {
        Object farMedium = classFromLoader(Size.class, loaderA).getEnumConstants()[1];

        assertSame(Size.MEDIUM, enumTranslator.encapsulate(farMedium));
    }

    public void testTranslatesSystemEnumInstancesIntoTheSameVersion() throws Exception {
        assertSame(Thread.State.RUNNABLE, enumTranslator.encapsulate(Thread.State.RUNNABLE));
    }
}
