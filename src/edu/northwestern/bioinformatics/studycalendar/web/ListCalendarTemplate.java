package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Jaron Sampson
 */

public class ListCalendarTemplate {
    private PlannedSchedule schedule;
    private List<CalendarEpoch> epochs;
    
    public ListCalendarTemplate(PlannedSchedule schedule) {
        this.schedule = schedule;
        epochs = new LinkedList<CalendarEpoch>();
        for (Epoch epoch : schedule.getEpochs()) {
            epochs.add(new CalendarEpoch(epoch));
        }
    }

    public String getName() {
        return schedule.getStudy().getName();
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
        List<DayOfPeriod> periods;
        Collection<Arm> arms;
        Integer dayNumber;

        public Day(int dayNumber, Collection<Arm> arms) {
            this.periods = new LinkedList<DayOfPeriod>();
            this.arms = arms;
            this.dayNumber = dayNumber;
            
            for (Arm arm : arms) {
                for (Period period : arm.getPeriods()) {
                    if (period.getDayRange().containsInteger(dayNumber)) {
                        periods.add(new DayOfPeriod(period, dayNumber));
                    }            	
	            }
	        }
        }

        public List<DayOfPeriod> getPeriods() {
            return periods;
        }
            
        public Collection<Arm> getArms() {
            return arms;
        }
        
        public Integer getDayNumber() {
        	return dayNumber;
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