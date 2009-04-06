package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.awt.*;

/**
 * @author Rhett Sutphin
 */
public class DefaultHat implements Hat {
    private Color color;

    public DefaultHat() { }

    public DefaultHat(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
