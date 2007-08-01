package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Iterator;

/**
 * @author Nataliya Shurupova
 */


public class HolidaysCommand {
    private static final Logger log = Logger.getLogger(HolidaysCommand.class.getName());
    private Integer selectedHoliday;
    private String holidayDate;
    private String holidayDescription;

    private Site site;
    private String action;
    private SiteDao siteDao;

    private Integer day;
    private Integer month;
    private Integer year;

    private Integer week;

    private String dayOfTheWeek;

    public HolidaysCommand(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public void execute() {
        if (getAction().equals("Remove")) {
            List<BlackoutDate> list = getSite().getHolidaysAndWeekends();
            for (Iterator<BlackoutDate> iterator = list.iterator(); iterator.hasNext();) {
                BlackoutDate abstractHolidayState =  iterator.next();
                if(abstractHolidayState.getId().equals(getSelectedHoliday())) {
                    iterator.remove();
                    siteDao.save(getSite());
                }

            }
        } else if (getAction().equals("Add")) {
            List<BlackoutDate> list = getSite().getHolidaysAndWeekends();
            BlackoutDate toAdd = null;
            if (getHolidayDate() != null ) {
                parse(getHolidayDate());
                MonthDayHoliday holiday = new MonthDayHoliday();
                holiday.setDay(getDay());
                holiday.setMonth(getMonth());
                holiday.setYear(getYear());
                toAdd = holiday;
            } else if (getDayOfTheWeek() != null && getWeek() == null && getMonth() == null){
                DayOfTheWeek dayOfTheWeek = new DayOfTheWeek();
                dayOfTheWeek.setDayOfTheWeek(getDayOfTheWeek());
                toAdd = dayOfTheWeek;
            } else {
                RelativeRecurringHoliday relativeRecurringHoliday = new RelativeRecurringHoliday();
                relativeRecurringHoliday.setWeekNumber(getWeek());
                relativeRecurringHoliday.setMonth(getMonth());
                relativeRecurringHoliday.setDayOfTheWeek(getDayOfTheWeek());
                toAdd = relativeRecurringHoliday;
            }
            if (getHolidayDescription()==null || getHolidayDescription().equals("")) {
                setHolidayDescription("Office is Closed");
            }
            toAdd.setDescription(getHolidayDescription());
            if(!isElementInTheList(list, toAdd)) {

                list.add(toAdd);
                siteDao.save(getSite());
            }
        }
    }

    public void parse (String date){
        String[] components = date.split("/");
        setDay(new Integer(components[1]));
        //we need to store month as Calendar class - where January ==0
        setMonth(new Integer(components[0])-1);
        if(components.length ==3){
            setYear(new Integer(components[2]));
        } else {
            setYear(null);
        }
    }

    public boolean isElementInTheList(List<BlackoutDate> list, BlackoutDate value) {
        for (Iterator<BlackoutDate> iterator = list.iterator(); iterator.hasNext();) {
            BlackoutDate abstractHolidayState = iterator.next();
            if(abstractHolidayState.equals(value)){
                return true;
            }
        }
        return false;
    }

    ////// BOUND PROPERTIES

    public Integer getSelectedHoliday() {
        return selectedHoliday;
    }

    public void setSelectedHoliday(Integer selectedHoliday) {
        this.selectedHoliday = selectedHoliday;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getHolidayDescription() {
        return holidayDescription;
    }

    public void setHolidayDescription(String holidayDescription) {
        this.holidayDescription = holidayDescription;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }    
}

