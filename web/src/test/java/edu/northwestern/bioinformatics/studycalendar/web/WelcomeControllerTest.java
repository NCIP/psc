/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;

/**
 * A dummy test to get emma running
 * 
 * @author Moses Hohman
 */
public class WelcomeControllerTest extends TestCase {
    public void testViewNameCorrect() {
        assertEquals("welcome", new WelcomeController().getViewName());
    }
}
