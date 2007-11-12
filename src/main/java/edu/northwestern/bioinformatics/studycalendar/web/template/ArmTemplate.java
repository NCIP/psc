package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.nwu.bioinformatics.commons.CollectionUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingMap;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ArmTemplate {
    private static final int MONTH_LENGTH = 28;

    private Arm arm;
    private List<Month> months;

    public ArmTemplate(Arm arm) {
        this.arm = arm;
        DayRange range = arm.getDayRange();
        int monthCount = (int) Math.ceil(((double) range.getDayCount()) / MONTH_LENGTH);
        months = new ArrayList<Month>(monthCount);
        while (months.size() < monthCount) {
            months.add(new Month(range.getStartDay() + MONTH_LENGTH * months.size()));
        }
    }

    public List<Month> getMonths() {
        return months;
    }

    public Arm getBase() {
        return arm;
    }

    public boolean getHasEvents() {
        for (Period period : arm.getPeriods()) {
            if (period.getPlannedActivities().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[arm=").append(getBase().getQualifiedName())
            .append("; months=").append(getMonths()).append(']').toString();
    }

    public class Month {
        private SortedMap<Integer, Day> days;
        private List<MonthOfPeriod> periods;

        public Month(int firstDay) {
            days = new TreeMap<Integer, Day>();
            int monthLength = Math.min(MONTH_LENGTH, arm.getDayRange().getEndDay() - firstDay + 1);
            while (days.size() < monthLength) {
                int dayN = firstDay + days.size();
                days.put(dayN, new Day(dayN));
            }
            initPeriods();
        }

        private void initPeriods() {
            // temporary structure, indexed by Period#id.
            SortedMap<Integer, MonthOfPeriod> byPeriodId = new ExpandingMap<Integer, MonthOfPeriod>(
                new ExpandingMap.Filler<MonthOfPeriod>() {
                    public MonthOfPeriod createNew(Object key) { return new MonthOfPeriod(); }
                }
            );
            for (Day day : getDays().values()) {
                for (DayOfPeriod dayOfPeriod : day.getPeriods()) {
                    byPeriodId.get(dayOfPeriod.getId()).addDay(dayOfPeriod);
                }
            }
            periods = new ArrayList<MonthOfPeriod>(byPeriodId.values());
        }

        public SortedMap<Integer, Day> getDays() {
            return days;
        }

        public List<MonthOfPeriod> getPeriods() {
            return periods;
        }

        public String toString() {
            return new StringBuilder(getClass().getSimpleName())
                .append("[days=[").append(getDays().firstKey()).append(", ").append(getDays().lastKey())
                .append(']').toString();
        }
    }

    public class MonthOfPeriod {
        private List<DayOfPeriod> days;

        public MonthOfPeriod() {
            days = new ArrayList<DayOfPeriod>(MONTH_LENGTH);
        }

        void addDay(DayOfPeriod day) {
            days.add(day);
        }

        public int getId() {
            return getPeriod().getId();
        }

        public String getName() {
            return getPeriod().getDisplayName();
        }

        private Period getPeriod() {
            return getFirstDay().getPeriod();
        }

        private DayOfPeriod getFirstDay() {
            return CollectionUtils.firstElement(getDays());
        }

        public boolean isResume() {
            return !getFirstDay().isFirstDayOfSpan()
                && getPeriod().getTotalDayRange().containsDay(getFirstDay().getDay().getNumber());
        }

        public List<DayOfPeriod> getDays() {
            return days;
        }
    }

    public class Day {
        private int number;
        private List<DayOfPeriod> periods;

        public Day(int number) {
            this.number = number;
            periods = new ArrayList<DayOfPeriod>();
            for (Period period : arm.getPeriods()) {
                periods.add(new DayOfPeriod(this, period));
            }
        }

        public int getNumber() {
            return number;
        }

        public List<PlannedActivity> getEvents() {
            List<PlannedActivity> events = new ArrayList<PlannedActivity>();
            for (DayOfPeriod period : getPeriods()) {
                events.addAll(period.getEvents());
            }
            return events;
        }

        public List<DayOfPeriod> getPeriods() {
            return periods;
        }
    }

    public class DayOfPeriod {
        private Day day;
        private Period period;
        private List<PlannedActivity> events;

        public DayOfPeriod(Day day, Period period) {
            this.period = period;
            this.day = day;
            this.events = new LinkedList<PlannedActivity>();

            for (PlannedActivity pe : period.getPlannedActivities()) {
                if (pe.getDaysInArm().contains(day.getNumber())) {
                    events.add(pe);
                }
            }
        }

        public boolean isFirstDayOfSpan() {
            if (isInPeriod()) {
                return period.isFirstDayOfRepetition(day.getNumber());
            } else {
                return period.isLastDayOfRepetition(day.getNumber() - 1);
            }
        }

        public boolean isLastDayOfSpan() {
            if (isInPeriod()) {
                return period.isLastDayOfRepetition(day.getNumber());
            } else {
                return period.isFirstDayOfRepetition(day.getNumber() + 1);
            }
        }

        public boolean isInPeriod() {
            return period.getTotalDayRange().containsDay(day.getNumber());
        }

        public boolean isEmpty() {
            return events.isEmpty();
        }

        public String getName() {
            return period.getName();
        }

        public Integer getId() {
            return period.getId();
        }

        public List<PlannedActivity> getEvents() {
            return events;
        }

        Period getPeriod() {
            return period;
        }

        public Day getDay() {
            return day;
        }
    }
}
