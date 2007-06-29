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
    private Integer listTypeOfHolidays;
    private String holidayDate;
    private String holidayDescription;

    private Site site;
    private String action;
    private SiteDao siteDao;

    private Integer day;
    private Integer month;
    private Integer year;

    private String dayOfTheWeek;

    public HolidaysCommand(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public void execute() {
        if (getAction().equals("Remove")) {
            List<AbstractHolidayState> list = getSite().getHolidaysAndWeekends();
            for (Iterator<AbstractHolidayState> iterator = list.iterator(); iterator.hasNext();) {
                AbstractHolidayState abstractHolidayState =  iterator.next();
                if(abstractHolidayState.getId().equals(getListTypeOfHolidays())) {
                    iterator.remove();
                    siteDao.save(getSite());
                }

            }
        } else if (getAction().equals("Add")) {
            List<AbstractHolidayState> list = getSite().getHolidaysAndWeekends();
            AbstractHolidayState toAdd = null;
            if (getHolidayDate() != null ) {
                parse(getHolidayDate());
                Holiday holiday = new Holiday();
                holiday.setDay(getDay());
                holiday.setMonth(getMonth());
                holiday.setYear(getYear());
                toAdd = holiday;
            } else {
                DayOfTheWeek dayOfTheWeek = new DayOfTheWeek();
                dayOfTheWeek.setDayOfTheWeek(getDayOfTheWeek());
                toAdd = dayOfTheWeek;
            }
            if (getHolidayDescription()==null || getHolidayDescription().equals("")) {
                setHolidayDescription("Office is Closed");
            }
            toAdd.setStatus(getHolidayDescription());
            if(!isElementInTheList(list, toAdd)) {

                list.add(toAdd);
                siteDao.save(getSite());
            }
        }
    }

    public void parse (String date){
        String[] components = date.split("/");
        setDay(new Integer(components[1]));
        setMonth(new Integer(components[0]));
        if(components.length ==3){
            setYear(new Integer(components[2]));
        } else {
            setYear(null);
        }
    }

    public boolean isElementInTheList(List<AbstractHolidayState> list, AbstractHolidayState value) {
        for (Iterator<AbstractHolidayState> iterator = list.iterator(); iterator.hasNext();) {
            AbstractHolidayState abstractHolidayState = iterator.next();
            if(abstractHolidayState.equals(value)){
                return true;
            }
        }
        return false;
    }

    ////// BOUND PROPERTIES

    public Integer getListTypeOfHolidays() {
        return listTypeOfHolidays;
    }

    public void setListTypeOfHolidays(Integer holiday_to_delete) {
        this.listTypeOfHolidays = holiday_to_delete;
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
}

