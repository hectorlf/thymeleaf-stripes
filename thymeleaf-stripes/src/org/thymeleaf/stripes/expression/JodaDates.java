/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2013, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.stripes.expression;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.thymeleaf.util.Validate;


/**
 *   Utility class for performing Joda date operations.
 *   An object of this class is usually available in variable evaluation expressions with the name
 *   <tt>#joda</tt>.
 */
public final class JodaDates {

    private final Locale locale;


    public JodaDates(final Locale locale) {
        super();
        Validate.notNull(locale, "Locale cannot be null");
        this.locale = locale;
    }

    
    
    /**
     * create methods
     */
    public DateTime create(final Object year, final Object month) {
    	return create(year, month, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
    }
    public DateTime create(final Object year, final Object month, final Object day) {
    	return create(year, month, day, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
    }
    public DateTime create(final Object year, final Object month, final Object day, final Object hour, final Object minute) {
        return create(year, month, day, hour, minute, Integer.valueOf(0), Integer.valueOf(0));
    }
    public DateTime create(final Object year, final Object month, final Object day, final Object hour, final Object minute, final Object second) {
        return create(year, month, day, hour, minute, second, Integer.valueOf(0));
    }
    public DateTime create(final Object year, final Object month, final Object day, final Object hour, final Object minute, final Object second, final Object millisecond) {
    	if (year == null) throw new IllegalArgumentException("Parameter year cannot be null");
    	if (month == null) throw new IllegalArgumentException("Parameter month cannot be null");
    	if (day == null) throw new IllegalArgumentException("Parameter day cannot be null");
    	if (hour == null) throw new IllegalArgumentException("Parameter hour cannot be null");
    	if (minute == null) throw new IllegalArgumentException("Parameter minute cannot be null");
    	if (second == null) throw new IllegalArgumentException("Parameter second cannot be null");
    	if (millisecond == null) throw new IllegalArgumentException("Parameter millisecond cannot be null");
    	try {
    		return new DateTime(toInt(year), toInt(month), toInt(day), toInt(hour), toInt(minute), toInt(second), toInt(millisecond));
    	} catch(NumberFormatException nfe) {
    		throw new IllegalArgumentException("Parameters must be integers");
    	}
    }


    /**
     * createNow methods
     */
    public DateTime createNow() {
        return new DateTime();
    }
    public DateTime createNowForTimeZone(final Object timeZone) {
        return new DateTime(toTz(timeZone));
    }


    /**
     * createToday methods
     */
    public DateTime createToday() {
        return new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfDay(0);
    }
    public DateTime createTodayForTimeZone(final Object timeZone) {
        return new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfDay(0).withZone(toTz(timeZone));
    }

    
    
    /**
     * format methods
     * Defaults to Long Style
     */
    public String format(final DateTime target) {
    	if (target == null) return null;
    	DateTimeFormatter dtf = DateTimeFormat.forStyle("LL").withLocale(locale);
        return dtf.print(target);
    }
    public String format(final DateTime target, final String pattern) {
    	if (target == null || pattern == null) return null;
    	DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern).withLocale(locale);
        return dtf.print(target);
    }

    
    
    
    /**
     * monthName methods
     * Defaults to Long Style
     */
    public String monthName(final DateTime target) {
    	if (target == null) return null;
        return target.monthOfYear().getAsText(locale);
    }
    public String monthNameShort(final DateTime target) {
    	if (target == null) return null;
        return target.monthOfYear().getAsShortText(locale);
    }

    
    
    
    /**
     * dayOfWeek methods
     * Defaults to Long Style
     */
    public String dayOfWeekName(final DateTime target) {
    	if (target == null) return null;
        return target.dayOfWeek().getAsText(locale);
    }
    public String dayOfWeekNameShort(final DateTime target) {
    	if (target == null) return null;
        return target.dayOfWeek().getAsShortText(locale);
    }

    
    
    
    
    /*
     * Utility methods
     */
    
    private int toInt(Object o) {
    	if (o instanceof Number) return ((Number)o).intValue();
		return Integer.parseInt(o.toString());
    }
    
    private DateTimeZone toTz(Object o) {
    	if (o instanceof TimeZone) return DateTimeZone.forTimeZone((TimeZone)o);
		return DateTimeZone.forID(o.toString());
    }
    
}