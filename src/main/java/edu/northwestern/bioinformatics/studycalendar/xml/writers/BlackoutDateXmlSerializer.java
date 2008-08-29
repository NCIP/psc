package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<BlackoutDate> {

    private Site site;

    public BlackoutDateXmlSerializer(final Site site) {
        if (site == null) {
            throw new StudyCalendarValidationException("site can not be null");
        }
        this.site = site;
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.BLACKOUT_DATES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.BLACKOUT_DATE;
    }

    @Override
    protected Element createElement(final BlackoutDate blackoutDate, final boolean inCollection) {
        if (blackoutDate == null) {
            throw new StudyCalendarValidationException("blackoutDate can not be null");

        }
        Element blackoutDateElement = rootElement().create();

        XsdAttribute.BLACKOUT_DATE_ID.addTo(blackoutDateElement, blackoutDate.getId());
        XsdAttribute.BLACKOUT_DATE_DESC.addTo(blackoutDateElement, blackoutDate.getDescription());
        XsdAttribute.BLACKOUT_DATE_SITE_ID.addTo(blackoutDateElement, site.getId());

        if (blackoutDate instanceof DayOfTheWeekBlackoutDate) {
            DayOfTheWeekBlackoutDate dayOfTheWeek = (DayOfTheWeekBlackoutDate) blackoutDate;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, dayOfTheWeek.getDayOfTheWeek());

        } else if (blackoutDate instanceof MonthDayBlackoutDate) {
            MonthDayBlackoutDate monthDayHoliday = (MonthDayBlackoutDate) blackoutDate;

            XsdAttribute.BLACKOUT_DATE_DAY.addTo(blackoutDateElement, monthDayHoliday.getDay());

            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, monthDayHoliday.getMonth());

            XsdAttribute.BLACKOUT_DATE_YEAR.addTo(blackoutDateElement, monthDayHoliday.getYear());

        } else if (blackoutDate instanceof RelativeRecurringBlackoutDate) {
            RelativeRecurringBlackoutDate relativeRecurringHoliday = (RelativeRecurringBlackoutDate) blackoutDate;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, relativeRecurringHoliday.getDayOfTheWeek());
            XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.addTo(blackoutDateElement, relativeRecurringHoliday.getWeekNumber());
            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, relativeRecurringHoliday.getMonth());

        }

        if (inCollection) {
            return blackoutDateElement;

        } else {
            Element root = collectionRootElement().create();
            root.add(blackoutDateElement);

            return root;
        }

    }

    @Override
    public BlackoutDate readElement(Element element) {
        if (element == null) {
            throw new StudyCalendarValidationException("element can not be null");

        }
        String holidayId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

        if (holidayId == null) {
            //create a new blackoutDate
            BlackoutDate blackoutDate = null;
            final String blackOutDateId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

            final String dayOfWeek = XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.from(element);
            final String day = XsdAttribute.BLACKOUT_DATE_DAY.from(element);

            final String month = XsdAttribute.BLACKOUT_DATE_MONTH.from(element);
            final String year = XsdAttribute.BLACKOUT_DATE_YEAR.from(element);
            final String weekNumber = XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.from(element);
            final String dateDesc = XsdAttribute.BLACKOUT_DATE_DESC.from(element);

            if (dayOfWeek != null && !dayOfWeek.trim().equals("")) {
                blackoutDate = new DayOfTheWeekBlackoutDate();
                ((DayOfTheWeekBlackoutDate) blackoutDate).setDayOfTheWeek(dayOfWeek);

            } else if (month != null || day != null || year != null) {
                blackoutDate = new MonthDayBlackoutDate();
                if (day != null && !day.trim().equalsIgnoreCase("")) {

                    ((MonthDayBlackoutDate) blackoutDate).setDay(Integer.parseInt(day));
                }
                if (month != null && !month.trim().equalsIgnoreCase("")) {

                    ((MonthDayBlackoutDate) blackoutDate).setMonth(Integer.parseInt(month));
                }
                if (year != null && !year.trim().equalsIgnoreCase("")) {

                    ((MonthDayBlackoutDate) blackoutDate).setYear(Integer.parseInt(year));
                }
            } else if (weekNumber != null || dayOfWeek != null || month != null) {
                blackoutDate = new RelativeRecurringBlackoutDate();
                ((RelativeRecurringBlackoutDate) blackoutDate).setDayOfTheWeek(dayOfWeek);
                if (month != null && !month.trim().equalsIgnoreCase("")) {
                    ((RelativeRecurringBlackoutDate) blackoutDate).setMonth(Integer.parseInt(month));
                }
                if (weekNumber != null && !weekNumber.trim().equalsIgnoreCase("")) {

                    ((RelativeRecurringBlackoutDate) blackoutDate).setWeekNumber(Integer.parseInt(weekNumber));
                }

            }
            if (blackoutDate != null) {
                blackoutDate.setDescription(dateDesc);
            }
            return blackoutDate;
        }

        List<BlackoutDate> blackoutDates = site.getBlackoutDates();
        for (BlackoutDate blackoutDate : blackoutDates) {
            if (blackoutDate.getId() != null && holidayId.equals(blackoutDate.getId().intValue() + "")) {
                return blackoutDate;
            }
        }


        throw new StudyCalendarValidationException("No Holday existis with id:" + holidayId + " at the site:" + site.getAssignedIdentifier());

    }

}
