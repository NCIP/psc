/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.DayNumber;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DayRange;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazySortedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class StudySegmentTemplate {
    private static final int MONTH_LENGTH = 28;

    private StudySegment studySegment;
    private List<Month> months;

    public StudySegmentTemplate(StudySegment studySegment) {
        this.studySegment = studySegment;
        DayRange range = studySegment.getDayRange();
        int monthCount = (int) Math.ceil(((double) range.getDayCount()) / MONTH_LENGTH);
        months = new ArrayList<Month>(monthCount);
        while (months.size() < monthCount) {
            months.add(new Month(range.getStartDay() + MONTH_LENGTH * months.size()));
        }
    }

    public List<Month> getMonths() {
        return months;
    }

    public StudySegment getBase() {
        return studySegment;
    }

    public boolean getHasEvents() {
        for (Period period : studySegment.getPeriods()) {
            if (period.getPlannedActivities().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[studySegment=").append(getBase().getQualifiedName())
            .append("; months=").append(getMonths()).append(']').toString();
    }

    public class Month {
        private SortedMap<Integer, Day> days;
        private List<MonthOfPeriod> periods;

        public Month(int firstDay) {
            days = new TreeMap<Integer, Day>();
            int monthLength = Math.min(MONTH_LENGTH, studySegment.getDayRange().getEndDay() - firstDay + 1);
            while (days.size() < monthLength) {
                int dayN = firstDay + days.size();
                days.put(dayN, new Day(dayN));
            }
            initPeriods();
        }

        private void initPeriods() {
            // temporary structure, indexed by Period#id.
            SortedMap<Integer, MonthOfPeriod> byPeriodId
                = LazySortedMap.decorate(new TreeMap<Integer, MonthOfPeriod>(), new Factory<MonthOfPeriod>() {
                    public MonthOfPeriod create() { return new MonthOfPeriod(); }
                });
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
                && getPeriod().getTotalDayRange().containsDay(getFirstDay().getDay().getNumber().getDayNumber());
        }

        public List<DayOfPeriod> getDays() {
            return days;
        }
    }

    public class Day {
        private DayNumber number;
        private List<DayOfPeriod> periods;

        public Day(int number) {
            this.number = DayNumber.createCycleDayNumber(number, studySegment.getCycleLength());
            periods = new ArrayList<DayOfPeriod>();
            for (Period period : studySegment.getPeriods()) {
                periods.add(new DayOfPeriod(this, period));
            }
        }

        public DayNumber getNumber() {
            return number;
        }

        public List<PlannedActivity> getEvents() {
            List<PlannedActivity> events = new ArrayList<PlannedActivity>();
            for (DayOfPeriod period : getPeriods()) {
                events.addAll(period.getEvents());
            }
            Collections.sort(events);
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
                if (pe.getDaysInStudySegment().contains(getAbsoluteDayNumber())) {
                    events.add(pe);
                }
            }
        }

        public boolean isFirstDayOfSpan() {
            if (isInPeriod()) {
                return period.isFirstDayOfRepetition(getAbsoluteDayNumber());
            } else {
                return period.isLastDayOfRepetition(getAbsoluteDayNumber() - 1);
            }
        }

        public boolean isLastDayOfSpan() {
            if (isInPeriod()) {
                return period.isLastDayOfRepetition(getAbsoluteDayNumber());
            } else {
                return period.isFirstDayOfRepetition(getAbsoluteDayNumber() + 1);
            }
        }

        private int getAbsoluteDayNumber() {
            return day.getNumber().getAbsoluteDayNumber();
        }

        public boolean isInPeriod() {
            return period.getTotalDayRange().containsDay(getAbsoluteDayNumber());
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
