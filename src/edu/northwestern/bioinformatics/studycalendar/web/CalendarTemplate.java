package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import org.apache.commons.lang.math.IntRange;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * It may be worthwhile to use this as a true domain object later.  (Although maybe not.)
 * For now, though, it is only used in the web tier.
 *
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class CalendarTemplate {
    private PlannedCalendar calendar;
    private List<CalendarEpoch> epochs;
    private Map<String, Integer> armClasses;
    private Map<String, Integer> periodClasses;

    public CalendarTemplate(PlannedCalendar calendar) {
        this.calendar = calendar;

        epochs = new LinkedList<CalendarEpoch>();
        for (Epoch epoch : calendar.getEpochs()) {
            epochs.add(new CalendarEpoch(epoch));
        }

        armClasses = new HashMap<String, Integer>();
        periodClasses = new HashMap<String, Integer>();
    }

    public String getName() {
        return calendar.getStudy().getName();
    }

    public List<CalendarEpoch> getEpochs() {
        return epochs;
    }

    private String getArmClass(Arm arm) {
        String key = arm.getName();
        if (!armClasses.containsKey(key)) {
            armClasses.put(key, armClasses.size());
        }
        return "arm" + armClasses.get(key);
    }

    private String getPeriodClass(Period period) {
        String key = period.getName();
        if (!periodClasses.containsKey(key)) {
            periodClasses.put(key, periodClasses.size());
        }
        return "period" + periodClasses.get(key);
    }

    // TODO: move to utility class
    private static IntRange intersect(IntRange r1, IntRange r2) {
        if (r1.containsRange(r2)) {
            return r2;
        } else if (r2.containsRange(r1)) {
            return r1;
        } else if (r1.overlapsRange(r2)) {
            return new IntRange(
                Math.max(r1.getMinimumInteger(), r2.getMinimumInteger()),
                Math.min(r1.getMaximumInteger(), r2.getMaximumInteger())
            );
        } else {
            return null;
        }
    }

    public class CalendarEpoch {
        private Epoch epoch;
        private List<Week> weeks;

        public CalendarEpoch(Epoch epoch) {
            this.epoch = epoch;

            int weekCount = (int) Math.ceil(epoch.getLengthInDays() / 7.0);
            weeks = new LinkedList<Week>();
            while (weeks.size() < weekCount) {
                weeks.add(new Week(weeks.size() * 7 + 1, epoch.getArms()));
            }
        }

        public String getName() {
            return epoch.getName();
        }

        public List<Week> getWeeks() {
            return weeks;
        }
    }

    public class Week {
        private List<WeekOfArm> arms;
        private IntRange range;

        public Week(int startDay, List<Arm> srcArms) {
            range = new IntRange(startDay, startDay + 6);
            arms = new LinkedList<WeekOfArm>();
            for (Arm arm : srcArms) {
                if (arm.getDayRange().overlapsRange(range)) {
                    arms.add(new WeekOfArm(arm, range));
                }
            }
        }

        public List<WeekOfArm> getArms() {
            return arms;
        }

        // for testing
        IntRange getRange() {
            return range;
        }
    }

    public class WeekOfArm {
        private Arm arm;
        private List<Day> days;

        public WeekOfArm(Arm arm, IntRange daysOfWeek) {
            this.arm = arm;
            days = new LinkedList<Day>();
            for (int d = daysOfWeek.getMinimumInteger() ; d <= daysOfWeek.getMaximumInteger() ; d++) {
                if (arm.getDayRange().containsInteger(d)) {
                    days.add(new Day(d, arm.getPeriods()));
                }
            }
        }

        public List<Day> getDays() {
            return days;
        }

        public String getCssClass() {
            return getArmClass(arm);
        }

        public String getName() {
            return arm.getName();
        }
    }

    public class Day {
        List<DayOfPeriod> periods;

        public Day(int dayNumber, Collection<Period> periodsOfArm) {
            this.periods = new LinkedList<DayOfPeriod>();
            for (Period period : periodsOfArm) {
                if (period.getDayRange().containsInteger(dayNumber)) {
                    periods.add(new DayOfPeriod(period, dayNumber));
                }
            }
        }

        public List<DayOfPeriod> getPeriods() {
            return periods;
        }
    }

    public class DayOfPeriod {
        private Period period;
        List<PlannedEvent> plannedEvents;

        public DayOfPeriod(Period period, int dayNumber) {
            this.period = period;
            this.plannedEvents = new LinkedList<PlannedEvent>();

            for (PlannedEvent pe : period.getPlannedEvents()) {
                if (pe.getDaysInArm().contains(dayNumber)) {
                    plannedEvents.add(pe);
                }
            }
        }

        public String getCssClass() {
            return getPeriodClass(period);
        }

        public String getName() {
            return period.getName();
        }

        public Integer getId() {
            return period.getId();
        }

        public List<PlannedEvent> getPlannedEvents() {
            return plannedEvents;
        }
    }
}
