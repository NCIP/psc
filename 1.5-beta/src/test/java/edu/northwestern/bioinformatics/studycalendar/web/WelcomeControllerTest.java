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
