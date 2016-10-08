package com.jebstern.matkakorttilukija;

import java.util.Calendar;
import java.util.Date;

/**
 * The Utilities class contains conversion utilities for hex string and date conversions from the ticket data.
 */

public class Utilities {

    /**
     * The en1545 format zero date (1.1.1997) in java Date format (number of milliseconds since 1.1.1970).
     */
    private static long en1545zeroDate = 852076800000L;

    /**
     * The length of one day in milliseconds.
     */
    private static long dayInMs = 86400000L;


    static byte[] selectHslCommand = {(byte) 0x90, (byte) 0x5A, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x11, (byte) 0x20, (byte) 0xEF, (byte) 0x00};
    static byte[] readAppinfoCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] readPeriodpassCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] readStoredvalueCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] readETicketCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] readHistoryCommand = {(byte) 0x90, (byte) 0xBB, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] readNextCommand = {(byte) 0x90, (byte) 0xAF, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    static byte[] OK = {(byte) 0x91, (byte) 0x00};
    static byte[] MORE_DATA = {(byte) 0x91, (byte) 0xAF};


    /**
     * Gets the hex string.
     *
     * @param b the Byte buffer to convert
     * @return String representing the hex values of the given bytes
     */
    public static String getHexString(byte[] b) {
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

    /**
     * En5145 date to java Date conversion.
     *
     * @param date the date in the en5145 format (number of days since 1.1.1997)
     * @return the date in java Date format
     */
    public static java.util.Date en5145Date2JavaDate(short date) {
        Calendar cal = Calendar.getInstance();
        int utcOffset = cal.getTimeZone().getOffset(((long) date * dayInMs) + en1545zeroDate);
        Date utcDate = new Date(((long) date * dayInMs) + en1545zeroDate - (long) utcOffset);

        return utcDate;
    }

    /**
     * Java Date to en5145 date conversion.
     *
     * @param date the java Date to convert
     * @return the date in en5145 format (number of days since 1.1.1997)
     */
    public static short JavaDate2en5145Date(Date date) {
        return (short) ((date.getTime() - en1545zeroDate) / dayInMs);
    }

    /**
     * En5145 date and time to java Date conversion
     *
     * @param date the date in the en5145 format (number of days since 1.1.1997)
     * @param time the time in en1545 format (number of minutes since 00:00)
     * @return the date  (with time) in java Date format
     */
    public static Date en5145DateAndTime2JavaDate(short date, short time) {
        Calendar cal = Calendar.getInstance();
        /*
      The length of one minute in milliseconds.
     */
        long minuteInMs = 60000L;
        int utcOffset = cal.getTimeZone().getOffset(((long) date * dayInMs) + en1545zeroDate + ((long) time * minuteInMs));
        Date utcDate = new Date(((long) date * dayInMs) + en1545zeroDate + ((long) time * minuteInMs) - (long) utcOffset);

        return utcDate;
    }
}
