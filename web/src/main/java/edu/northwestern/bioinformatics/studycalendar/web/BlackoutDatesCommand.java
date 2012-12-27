/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Iterator;

/**
 * @author Nataliya Shurupova
 */


public class BlackoutDatesCommand {
    private static final Logger log = LoggerFactory.getLogger(BlackoutDatesCommand.class.getName());
    private Integer selectedHoliday;
    private String holidayDate;
    private String holidayDescription;

    private Site site;
    private String action;
    private SiteDao siteDao;
    private BlackoutDateDao blackoutDateDao;

    private Integer day;
    private Integer month;
    private Integer year;

    private Integer week;

    private String dayOfTheWeek;

    public BlackoutDatesCommand(SiteDao siteDao, BlackoutDateDao blackoutDateDao) {
        this.siteDao = siteDao;
        this.blackoutDateDao = blackoutDateDao;
    }

    public void execute() {
        if (getAction().equals("Remove")) {
            List<BlackoutDate> list = getSite().getBlackoutDates();
            for (Iterator<BlackoutDate> iterator = list.iterator(); iterator.hasNext();) {
                BlackoutDate abstractHolidayState =  iterator.next();
                if(abstractHolidayState.getId().equals(getSelectedHoliday())) {
                    iterator.remove();
                }

            }
        } else if (getAction().equals("Add")) {
            List<BlackoutDate> list = getSite().getBlackoutDates();
            BlackoutDate toAdd = null;
            if (getHolidayDate() != null ) {
                parse(getHolidayDate());
                SpecificDateBlackout holiday = new SpecificDateBlackout();
                holiday.setDay(getDay());
                holiday.setMonth(getMonth());
                holiday.setYear(getYear());
                toAdd = holiday;
            } else if (getDayOfTheWeek() != null && getWeek() == null && getMonth() == null){
                WeekdayBlackout dayOfTheWeek = new WeekdayBlackout();
                dayOfTheWeek.setDayOfTheWeek(getDayOfTheWeek());
                toAdd = dayOfTheWeek;
            } else {
                RelativeRecurringBlackout relativeRecurringHoliday = new RelativeRecurringBlackout();
                relativeRecurringHoliday.setWeekNumber(getWeek());
                relativeRecurringHoliday.setMonth(getMonth());
                relativeRecurringHoliday.setDayOfTheWeek(getDayOfTheWeek());
                toAdd = relativeRecurringHoliday;
            }
            if (getHolidayDescription()==null || getHolidayDescription().equals("")) {
                setHolidayDescription("Office is Closed");
            }
            toAdd.setDescription(getHolidayDescription());
            toAdd.setSite(getSite());
            if(!isElementInTheList(list, toAdd)) {
                blackoutDateDao.save(toAdd);
                list.add(toAdd);
                getSite().setBlackoutDates(list);
            }
        }
        siteDao.save(getSite());
    }

    public void parse (String date){
        String[] components = date.split("/");
        String configuredFormat =FormatTools.getLocal().getDateFormatString();
        if (configuredFormat.toLowerCase().startsWith("mm")) {
            setDay(new Integer(components[1]));
            //we need to store month as Calendar class - where January ==0
            setMonth(new Integer(components[0])-1);
        } else {
            setDay(new Integer(components[0]));
            setMonth(new Integer(components[1])-1);
        }
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

