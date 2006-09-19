package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Jaron Sampson
 */

public class ListCalendarTemplate {
    private PlannedCalendar calendar;
    private List<CalendarEpoch> epochs;
    
    public ListCalendarTemplate(PlannedCalendar calendar) {
        this.calendar = calendar;
        epochs = new LinkedList<CalendarEpoch>();
        for (Epoch epoch : calendar.getEpochs()) {
            epochs.add(new CalendarEpoch(epoch));
        }
    }

    public String getName() {
        return calendar.getStudy().getName();
    }
    
    public List<CalendarEpoch> getEpochs() {
        return epochs;
    }    
    
    public class CalendarEpoch {
        private Epoch epoch;
        private List<Day> days;

        public CalendarEpoch(Epoch epoch) {
            this.epoch = epoch;
            days = new LinkedList<Day>();
            
            while (days.size() < epoch.getLengthInDays()) {
                days.add(new Day(days.size() + 1, epoch.getArms()));
            }
        }

        public String getName() {
            return epoch.getName();
        }

        public List<Day> getDays() {
            return days;
        }
    }
    
    public class Day {

        List<DayOfArm> arms;
        Integer dayNumber;

        public Day(int dayNumber, Collection<Arm> arms) {

            this.arms = new LinkedList<DayOfArm>();
            this.dayNumber = dayNumber;
            
            for (Arm arm : arms) {            	
                this.arms.add(new DayOfArm(arm, dayNumber));
	        }
        }

            
        public List<DayOfArm> getArms() {
            return arms;
        }
        
        public Integer getDayNumber() {
        	return dayNumber;
        }


    }
    
    public class DayOfArm {
    	Arm arm;
        List<DayOfPeriod> periods;
        
        public DayOfArm(Arm arm, Integer dayNumber) {
            this.periods = new LinkedList<DayOfPeriod>();
            this.arm = arm;
            
            for (Period period : arm.getPeriods()) {
                if (period.getDayRange().containsInteger(dayNumber)) {
                    periods.add(new DayOfPeriod(period, dayNumber));
                }            	
            }            
        }
        
        public List<DayOfPeriod> getPeriods() {
            return periods;
        }
        
        public String getName() {
            return arm.getName();
        }
    }

    public class DayOfPeriod {
        private Period period;
        private List<PlannedEvent> plannedEvents;
        
        
        
        public DayOfPeriod(Period period, Integer dayNumber) {
            this.period = period;
            this.plannedEvents = new LinkedList<PlannedEvent>();
            
            for (PlannedEvent pe : period.getPlannedEvents()) {
                if (pe.getDaysInArm().contains(dayNumber)) {
                    plannedEvents.add(pe);
                }
            }
        }
        
        public List<PlannedEvent> getPlannedEvents() {
            return plannedEvents;
        }

        public String getName() {
            return period.getName();
        }

        public Integer getId() {
            return period.getId();
        }
    }
}