package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.Type;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@Embeddable
public class Duration implements Comparable<Duration>, Cloneable {
    public static enum Unit {
        day(1), week(7), fortnight(14), month(28), quarter(91);
        private int inDays;

        private Unit(int inDays) {
            this.inDays = inDays;
        }

        public int inDays() {
            return inDays;
        }
    }

    private Integer quantity;
    @Type(type="durationUnit")
    private Unit unit;

    public Duration() {
        this(null, null);
    }

    public Duration(Integer quantity, Unit unit) {
        setQuantity(quantity);
        setUnit(unit);
    }

    @Transient
    public Integer getDays() {
        if (unit == null || quantity == null) return null;
        return quantity * unit.inDays();
    }

    public int compareTo(Duration o) {
        return ComparisonTools.nullSafeCompare(this.getDays(), o.getDays()); 
    }

    // Bean methods

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity != null && quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        this.quantity = quantity;
    }

    @Type(type="durationUnit")
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
       this.unit = unit;
    }

    public Differences deepEquals(Object o) {
        Differences differences = new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("not valid duration");
            return differences;
        }

        final Duration duration = (Duration) o;

        if (quantity != null ? !quantity.equals(duration.quantity) : duration.quantity != null) {
            differences.addMessage(String.format("duration quantity %d differs to %d", quantity, duration.quantity));
        }
        if (unit != duration.unit) {
            differences.addMessage(String.format("duration unit %s differs to %s", unit, duration.unit));
        }
        return differences;
    }

    // Object methods

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(quantity).append(' ');
        if (unit == null) {
            result.append("(null unit)");
        } else {
            result.append(unit);
        }
        if (quantity == null || quantity != 1) {
            result.append('s');
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Duration duration = (Duration) o;

        if (quantity != null ? !quantity.equals(duration.getQuantity()) : duration.getQuantity() != null) return false;
        if (unit != duration.unit) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (quantity != null ? quantity.hashCode() : 0);
        result = 29 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }


    @Override
    protected Duration clone() {
        try {
            return (Duration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("It is supported", e);
        }
    }
}
