package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.annotations.Type;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@Embeddable
public class Duration implements Comparable<Duration> {
    public static enum Unit {
        day(1), week(7);
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
        return ComparisonUtils.nullSafeCompare(this.getDays(), o.getDays()); 
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

    // Object methods

    public String toString() {
        StringBuilder result = new StringBuilder().append(quantity).append(" ");
        if (unit == null) {
            result.append("(null unit)");
        } else {
            result.append(unit);
        }
        if (quantity == null || quantity != 1) {
            result.append("s");
        }
        return result.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Duration duration = (Duration) o;

        if (quantity != null ? !quantity.equals(duration.quantity) : duration.quantity != null) return false;
        if (unit != duration.unit) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (quantity != null ? quantity.hashCode() : 0);
        result = 29 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
