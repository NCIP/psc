package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.DayOfTheWeek;
import edu.northwestern.bioinformatics.studycalendar.domain.Holiday;
import edu.northwestern.bioinformatics.studycalendar.domain.MonthDayHoliday;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringHoliday;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Holiday> {

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
    protected Element createElement(final Holiday holiday, final boolean inCollection) {
        if (holiday == null) {
            throw new StudyCalendarValidationException("holiday can not be null");

        }
        Element blackoutDateElement = rootElement().create();

        XsdAttribute.BLACKOUT_DATE_ID.addTo(blackoutDateElement, holiday.getId());
        XsdAttribute.BLACKOUT_DATE_DESCRIPTION.addTo(blackoutDateElement, holiday.getDescription());
        XsdAttribute.BLACKOUT_DATE_SITE_ID.addTo(blackoutDateElement, site.getId());

        if (holiday instanceof DayOfTheWeek) {
            DayOfTheWeek dayOfTheWeek = (DayOfTheWeek) holiday;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, dayOfTheWeek.getDayOfTheWeek());

        } else if (holiday instanceof MonthDayHoliday) {
            MonthDayHoliday monthDayHoliday = (MonthDayHoliday) holiday;

            XsdAttribute.BLACKOUT_DATE_DAY.addTo(blackoutDateElement, monthDayHoliday.getDay());

            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, monthDayHoliday.getMonth());

            XsdAttribute.BLACKOUT_DATE_YEAR.addTo(blackoutDateElement, monthDayHoliday.getYear());

        } else if (holiday instanceof RelativeRecurringHoliday) {
            RelativeRecurringHoliday relativeRecurringHoliday = (RelativeRecurringHoliday) holiday;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, relativeRecurringHoliday.getDayOfTheWeek());
            XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.addTo(blackoutDateElement, relativeRecurringHoliday.getWeekNumber());
            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, relativeRecurringHoliday.getMonth());

        }

        return blackoutDateElement;
    }

    @Override
    public Holiday readElement(Element element) {
        if (element == null) {
            throw new StudyCalendarValidationException("element can not be null");

        }
        String holidayId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

        if (holidayId == null) {
            //create a new holiday
            Holiday holiday = null;
            final String blackOutDateId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

            final String dayOfWeek = XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.from(element);
            final String day = XsdAttribute.BLACKOUT_DATE_DAY.from(element);

            final String month = XsdAttribute.BLACKOUT_DATE_MONTH.from(element);
            final String year = XsdAttribute.BLACKOUT_DATE_YEAR.from(element);
            final String weekNumber = XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.from(element);
            final String dateDesc = XsdAttribute.BLACKOUT_DATE_DESCRIPTION.from(element);

            if (dayOfWeek != null && !dayOfWeek.trim().equals("")) {
                holiday = new DayOfTheWeek();
                ((DayOfTheWeek) holiday).setDayOfTheWeek(dayOfWeek);

            } else if (month != null || day != null || year != null) {
                holiday = new MonthDayHoliday();
                if (day != null && !day.trim().equalsIgnoreCase("")) {

                    ((MonthDayHoliday) holiday).setDay(Integer.parseInt(day));
                }
                if (month != null && !month.trim().equalsIgnoreCase("")) {

                    ((MonthDayHoliday) holiday).setMonth(Integer.parseInt(month));
                }
                if (year != null && !year.trim().equalsIgnoreCase("")) {

                    ((MonthDayHoliday) holiday).setYear(Integer.parseInt(year));
                }
            } else if (weekNumber != null || dayOfWeek != null || month != null) {
                holiday = new RelativeRecurringHoliday();
                ((RelativeRecurringHoliday) holiday).setDayOfTheWeek(dayOfWeek);
                if (month != null && !month.trim().equalsIgnoreCase("")) {
                    ((RelativeRecurringHoliday) holiday).setMonth(Integer.parseInt(month));
                }
                if (weekNumber != null && !weekNumber.trim().equalsIgnoreCase("")) {

                    ((RelativeRecurringHoliday) holiday).setWeekNumber(Integer.parseInt(weekNumber));
                }

            }
            if (holiday != null) {
                holiday.setDescription(dateDesc);
            }
            return holiday;
        }

        List<Holiday> holidays = site.getHolidaysAndWeekends();
        for (Holiday holiday : holidays) {
            if (holiday.getId() != null && holidayId.equals(holiday.getId().intValue() + "")) {
                return holiday;
            }
        }


        throw new StudyCalendarValidationException("No Holday existis with id:" + holidayId + " at the site:" + site.getAssignedIdentifier());

    }

}
