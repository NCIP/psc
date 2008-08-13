package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * Representation of a day in a segment, with or without a cycle as context.
 * All day number & cycle number parameters are one-based.
 *
 * @author Rhett Sutphin
 */
public abstract class DayNumber {
    private int dayNumber;

    public static DayNumber create(int dayNumber) {
        return create(null, dayNumber);
    }

    public static DayNumber create(Integer cycleNumber, int dayNumber) {
        if (cycleNumber == null || dayNumber < 1) {
            return new DayNumber.WithoutCycle(dayNumber);
        } else {
            return new DayNumber.WithCycle(cycleNumber, dayNumber);
        }
    }

    public static DayNumber createCycleDayNumber(int absoluteDayNumber, Integer cycleLength) {
        if (cycleLength == null || absoluteDayNumber < 1) {
            return create(absoluteDayNumber);
        } else {
            return create(
                (absoluteDayNumber - 1) / cycleLength + 1,
                (absoluteDayNumber - 1) % cycleLength + 1);
        }
    }

    protected DayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public abstract String getCycleEvenOrOdd();

    @Override
    public abstract String toString();

    public static class WithCycle extends DayNumber {
        private int cycleNumber;

        private WithCycle(int cycleNumber, int dayNumber) {
            super(dayNumber);
            this.cycleNumber = cycleNumber;
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
    }

    public static class WithoutCycle extends DayNumber {
        private WithoutCycle(Integer dayNumber) {
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
    }
}
