/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * Representation of a day in a segment, with or without a cycle as context.
 * All day number & cycle number parameters are one-based.
 *
 * @author Rhett Sutphin
 */
public abstract class DayNumber {
    private int dayNumber;

    public static DayNumber createAbsoluteDayNumber(int dayNumber) {
        return create(null, dayNumber);
    }

    private static DayNumber create(Integer cycleNumber, int dayNumber) {
        if (cycleNumber == null || dayNumber < 1) {
            return new DayNumber.WithoutCycle(dayNumber);
        } else {
            return new DayNumber.WithCycle(cycleNumber, dayNumber);
        }
    }

    public static DayNumber createCycleDayNumber(int absoluteDayNumber, Integer cycleLength) {
        if (cycleLength == null || absoluteDayNumber < 1) {
            return createAbsoluteDayNumber(absoluteDayNumber);
        } else {
            return new WithCycle(absoluteDayNumber, cycleLength);
        }
    }

    protected DayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public boolean getHasCycle() {
        return getCycleEvenOrOdd() != null;
    }

    public abstract String getCycleEvenOrOdd();

    @Override
    public abstract String toString();

    public abstract int getAbsoluteDayNumber();

    public static class WithCycle extends DayNumber {
        private int cycleLength;
        private int cycleNumber;

        private WithCycle(int absoluteDayNumber, int cycleLength) {
            super((absoluteDayNumber - 1) % cycleLength + 1);
            this.cycleNumber = (absoluteDayNumber - 1) / cycleLength + 1;
            this.cycleLength = cycleLength;
        }

        public Integer getCycleNumber() {
            return cycleNumber;
        }

        @Override
        public String getCycleEvenOrOdd() {
            return cycleNumber % 2 == 0 ? "even" : "odd";
        }

        @Override
        public String toString() {
            return String.format("C%dD%d", cycleNumber, getDayNumber());
        }

        public int getAbsoluteDayNumber() {
            return (cycleNumber - 1) * cycleLength + getDayNumber();
        }
    }

    public static class WithoutCycle extends DayNumber {
        private WithoutCycle(int dayNumber) {
            super(dayNumber);
        }

        @Override
        public String getCycleEvenOrOdd() {
            return null;
        }

        @Override
        public String toString() {
            return Integer.toString(getDayNumber());
        }

        public int getAbsoluteDayNumber() {
            return getDayNumber();
        }
    }
}
