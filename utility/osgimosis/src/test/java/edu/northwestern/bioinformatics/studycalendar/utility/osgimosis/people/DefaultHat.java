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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hat)) return false;

        DefaultHat that = (DefaultHat) o;

        if (color != null ? !color.equals(that.getColor()) : that.getColor() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return color != null ? color.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append('[').append(getColor()).append(']').toString();
    }
}
