package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
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
 */
public class CalendarTemplate {
    private PlannedSchedule schedule;
    private List<Week> weeks;
    private Map<String, Integer> armClasses;
    private Map<String, Integer> periodClasses;

    public CalendarTemplate(PlannedSchedule schedule) {
        this.schedule = schedule;
        int weekCount = (int) Math.ceil(schedule.getLengthInDays() / 7.0);
        weeks = new LinkedList<Week>();
        while (weeks.size() < weekCount) {
            weeks.add(new Week(weeks.size() * 7 + 1));
        }

        armClasses = new HashMap<String, Integer>();
        periodClasses = new HashMap<String, Integer>();
    }

    public String getName() {
        return schedule.getStudy().getName();
    }

    public List<Week> getWeeks() {
        return weeks;
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

    public class Week {
        private List<WeekOfArm> arms;
        private IntRange range;

        public Week(int startDay) {
            range = new IntRange(startDay, startDay + 6);
            arms = new LinkedList<WeekOfArm>();
            // TODO:
//            for (Arm arm : schedule.getArms()) {
//                if (arm.getDayRange().overlapsRange(range)) {
//                    arms.add(new WeekOfArm(arm, range));
//                }
//            }
        }

        public List<WeekOfArm> getArms() {
            return arms;
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
                    periods.add(new DayOfPeriod(period));
                }
            }
        }

        public List<DayOfPeriod> getPeriods() {
            return periods;
        }
    }

    public class DayOfPeriod {
        private Period period;

        public DayOfPeriod(Period period) {
            this.period = period;
        }

        public String getCssClass() {
            return getPeriodClass(period);
        }

        public String getName() {
            return period.getName();
        }
    }
}
