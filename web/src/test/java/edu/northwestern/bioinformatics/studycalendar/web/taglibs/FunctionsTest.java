package edu.northwestern.bioinformatics.studycalendar.web.taglibs;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class FunctionsTest extends StudyCalendarTestCase {
    public void testPluck() throws Exception {
        Collection in = Arrays.asList(
            new Person("Steve"),
            new Person("Frank")
        );

        Iterator result = Functions.pluck(in, "name").iterator();

        assertEquals("Wrong first value", "Steve", result.next());
        assertEquals("Wrong second value", "Frank", result.next());
    }

    public void testEscapeJsUsingBackSlash() throws Exception {
        assertEquals("Wrong value", "\\\\", Functions.escapeJavascript("\\"));
    }

    public void testEscapeJsUsingXml() throws Exception {
        assertEquals("Wrong value", "<\\/", Functions.escapeJavascript("</"));
    }

    public void testEscapeJsUsingDoubleQuote() throws Exception {
        assertEquals("Wrong value", "\\\"", Functions.escapeJavascript("\""));
    }
    
    public void testEscapeJsUsingSingleQuote() throws Exception {
        assertEquals("Wrong value", "\\\'", Functions.escapeJavascript("\'"));
    }

    // Helper Class

    public static class Person {
        String name;

        private Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
