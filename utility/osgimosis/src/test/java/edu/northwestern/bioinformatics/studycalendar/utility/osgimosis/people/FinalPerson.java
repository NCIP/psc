package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.awt.*;

/**
 * @author Rhett Sutphin
 */
public class FinalPerson implements Person {
    private Hat hat = new DefaultHat(Color.BLACK);

    public final String getName() {
        return "MacLeod";
    }

    public final String getKind() {
        return "highlander";
    }

    public final Hat getHat() {
         return hat;
    }
}
