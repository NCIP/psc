/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.awt.*;

/**
 * @author Rhett Sutphin
 */
public class DefaultHat implements Hat {
    private Color color;
    private Size size;

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

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hat)) return false;

        DefaultHat that = (DefaultHat) o;

        if (color != null ? !color.equals(that.getColor()) : that.getColor() != null) return false;
        if (size != null ? !size.equals(that.getSize()) : that.getSize() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[color=").append(getColor()).
            append(", size=").append(getSize()).
            append(']').toString();
    }
}
