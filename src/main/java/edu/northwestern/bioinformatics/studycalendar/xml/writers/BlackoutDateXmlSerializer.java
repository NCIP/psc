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
        XsdAttribute.BLACKOUT_DATE_DESC.addTo(blackoutDateElement, holiday.getDescription());
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

        if (inCollection) {
            return blackoutDateElement;

        } else {
            Element root = collectionRootElement().create();
            root.add(blackoutDateElement);

            return root;
        }

    }

    @Override
    public Holiday readElement(Element element) {
        if (element == null) {
            throw new StudyCalendarValidationException("element can not be null");

        }
        String holidayId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

        if (holidayId == null) {
            throw new StudyCalendarValidationException("Element must have id attribute");
        }

        List<Holiday> holidays = site.getHolidaysAndWeekends();
        for (Holiday holiday : holidays) {
            if (holiday.getId() != null && holidayId.equals(holiday.getId().intValue() + "")) {
                return holiday;
            }
        }

        throw new StudyCalendarValidationException("No Holday existis with id:" + holidayId + " at the site:" + site.getId());

    }

}
