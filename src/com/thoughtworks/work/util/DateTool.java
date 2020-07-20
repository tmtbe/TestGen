package com.thoughtworks.work.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple date formatting tool for more flexibility in the template. This class could be expanded
 * if users have more sophisticated requirements
 *
 * @author Jon Osborn
 * @since 1/6/12 10:48 AM
 */
public class DateTool {

    public static final String DEFAULT_FORMAT = "MMM d, yyyy";

    /**
     * The current calendar instance
     *
     * @return the calendar
     */
    public Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * The current date
     *
     * @return the current date
     */
    public Date getDate() {
        return getCalendar().getTime();
    }


    /**
     * Format the 'current' date and locale with the specified format
     *
     * @param format the format
     * @return the string representation
     */
    public String format(String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(getDate());
    }

    /**
     * Return the current date as a basic date in the current locale
     *
     * @return the date
     */
    @Override
    public String toString() {
        return this.format(DEFAULT_FORMAT);
    }
}
