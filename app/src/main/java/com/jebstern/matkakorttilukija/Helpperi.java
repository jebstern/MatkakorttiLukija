package com.jebstern.matkakorttilukija;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Helpperi {

    private byte MINUTES = 0, HOURS, ALLDAY, DAYS;
    TravelCard travelCard;
    eTicket eticket;
    Context app_context;
    String cardNumber, period1Date, period1Status, period1Zone, period2Date, period2Status, period2Zone;
    String cardValue, loadedValue, history, ETicketValidity;
    String boardingDate;
    String loadingDate;
    short boardingVehicle, loadedPeriodLength, loadedPeriodProduct, loadingDeviceNumber, loadingOrganizationID, boardingLocationNum;
    byte boardingArea, loadingTime, boardingDirection, boardingLocationNumType, historyLength;
    int loadedPeriodPrice;
    long daysLeftValid1, daysLeftValid2;

    public Helpperi(TravelCard travelCard, eTicket eticket, Context app_context) {
        HOURS = 1;
        ALLDAY = 2;
        DAYS = 3;
        this.travelCard = travelCard;
        this.eticket = eticket;
        this.app_context = app_context;
    }


    public void setup() {
        cardNumber = travelCard.getApplicationInstanceId();
        TravelCardPeriodValidity1();
        TravelCardPeriodValidity2();
        TravelCardValue();
        LoadedValue();
        TravelCardHistory();
        ETicketValidity(eticket);
        InitOtherValues();
    }


    public void TravelCardPeriodValidity1() {
        String mPeriodZone = null;
        String mPeriodStatus = null;
        String mPeriodDate = null;

        //Set date format to be used on output string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

        //Current calendar
        Calendar currentCal = Calendar.getInstance();

        //Instantiate helper class to get names for the ticket's zone or vehicle type where the ticket is valid
        ValidityAreaMappings mappings = new ValidityAreaMappings(app_context);
        String validityArea = mappings.getValidityArea((int) travelCard.getValidityAreaType1(), (int) travelCard.getValidityArea1());

        //get Calendar instance of Period start date
        Calendar periodStartCal1 = Calendar.getInstance();
        periodStartCal1.setTime(travelCard.getPeriodStartDate1());

        //get Calendar instance of Period end date
        Calendar periodEndCal1 = Calendar.getInstance();
        periodEndCal1.setTime(travelCard.getPeriodEndDate1());

        //Check that we've got validity area and that period 1 exists (it's starting date is set)
        //If period 1 does not exist, it's data on the card is filled with zeroes and en1545 date with 0 value is converted to java Date 1.1.1997...
        if ((validityArea != null) && (periodStartCal1.get(Calendar.YEAR) > 1997)) {
            mPeriodZone = app_context.getResources().getString(R.string.zone) + ": " + validityArea;
            //if period is valid for now (no end date set)
            if (periodStartCal1.before(currentCal) && (periodEndCal1.get(Calendar.YEAR) == 1997)) {
                mPeriodStatus = app_context.getResources().getString(R.string.status);
            } else {
                mPeriodDate = dateFormat.format(periodStartCal1.getTime());

                //If period's end date is not set
                if (periodEndCal1.get(Calendar.YEAR) == 1997) {
                    //Indication of no ending date
                    mPeriodDate += app_context.getResources().getString(R.string.arrow);
                }
                //If eperido end date is set
                else {
                    //Write ending date into info string
                    mPeriodDate += " - " + dateFormat.format(periodEndCal1.getTime());
                    long daysleft = daysLeftValid(dateFormat.format(periodEndCal1.getTime()));
                    daysLeftValid1 = daysleft;
                }

                //Determine the status of the period 1
                //If period starting date is in the future
                if (periodStartCal1.after(currentCal)) {
                    //Set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.hasNotBegun);
                }
                //if period is currently valid
                else if (currentCal.before(periodEndCal1) && currentCal.after(periodStartCal1)) {
                    //Set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.validUntil);
                }
                //Othervise the period is not valid anymore
                else {
                    //set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.noLongerValid);
                }
            }
        }

        period1Date = mPeriodDate;
        period1Status = mPeriodStatus;
        period1Zone = mPeriodZone;

    }


    public void TravelCardPeriodValidity2() {
        String mPeriodZone = null;
        String mPeriodStatus = null;
        String mPeriodDate = null;

        //Set date format to be used on output string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

        //Current calendar
        Calendar currentCal = Calendar.getInstance();

        //Instantiate helper class to get names for the ticket's zone or vehicle type where the ticket is valid
        ValidityAreaMappings mappings = new ValidityAreaMappings(app_context);
        String validityArea = mappings.getValidityArea((int) travelCard.getValidityAreaType2(), (int) travelCard.getValidityArea2());

        //get Calendar instance of Period start date
        Calendar periodStartCal2 = Calendar.getInstance();
        periodStartCal2.setTime(travelCard.getPeriodStartDate2());

        //get Calendar instance of Period end date
        Calendar periodEndCal2 = Calendar.getInstance();
        periodEndCal2.setTime(travelCard.getPeriodEndDate2());

        //Check that we've got validity area and that period 1 exists (it's starting date is set)
        //If period 1 does not exist, it's data on the card is filled with zeroes and en1545 date with 0 value is converted to java Date 1.1.1997...
        if ((validityArea != null) && (periodStartCal2.get(Calendar.YEAR) > 1997)) {
            mPeriodZone = app_context.getResources().getString(R.string.zone) + ": " + validityArea;
            //if period is valid for now (no end date set)
            if (periodStartCal2.before(currentCal) && (periodEndCal2.get(Calendar.YEAR) == 1997)) {
                mPeriodStatus = app_context.getResources().getString(R.string.status);
            } else {
                mPeriodDate = dateFormat.format(periodStartCal2.getTime());

                //If period's end date is not set
                if (periodEndCal2.get(Calendar.YEAR) == 1997) {
                    //Indication of no ending date
                    mPeriodDate += app_context.getResources().getString(R.string.arrow);
                }
                //If eperido end date is set
                else {
                    //Write ending date into info string
                    mPeriodDate += " - " + dateFormat.format(periodEndCal2.getTime());
                    long daysleft = daysLeftValid(dateFormat.format(periodEndCal2.getTime()));
                    daysLeftValid2 = daysleft;
                }

                //Determine the status of the period 1
                //If period starting date is in the future
                if (periodStartCal2.after(currentCal)) {
                    //Set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.hasNotBegun);
                }
                //if period is currently valid
                else if (currentCal.before(periodEndCal2) && currentCal.after(periodStartCal2)) {
                    //Set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.isValid);
                }
                //Othervise the period is not valid anymore
                else {
                    //set status text
                    mPeriodStatus = app_context.getResources().getString(R.string.noLongerValid);
                }
            }
        }

        period2Date = mPeriodDate;
        period2Status = mPeriodStatus;
        period2Zone = mPeriodZone;
    }




    public void TravelCardValue() {
        //Calculate euros and cents out of value counter
        String euros = String.valueOf(travelCard.getValueCounter() / 100);
        String cents = String.valueOf(travelCard.getValueCounter() % 100);
        String value = euros + "." + cents + "\u20ac";
        cardValue = value;
    }


    public void LoadedValue() {
        int getLoadedValue = travelCard.getLoadedValue();
        double mLoadedValue = (double) getLoadedValue / 100;
        String value = mLoadedValue + "\u20ac";
        loadedValue = value;
    }


    public void TravelCardHistory() {
        //String to return
        String historyStr = "";
        //Calendar instance to get transaction times
        Calendar transactionTime = Calendar.getInstance();
        //Set date format to be used on output string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);

        //get the history data from the card
        TravelCard.History[] hist = travelCard.getHistory();

        //Show last 7 trips from history
        for (int i = travelCard.getHistoryLen() - 1; i >= 0; i--) {
            //get transaction date and time
            transactionTime.setTime(hist[i].getTransactionDateTime());

            //print date and time
            historyStr += dateFormat.format(transactionTime.getTime());

            //If this is season journey (0 = Season journey , 1 = Value ticket)
            if (hist[i].getTransactionType() == 0) {

                //Add transaction type "season" to string
                historyStr += " " + app_context.getResources().getString(R.string.periodTrip);
            }
            //This is value ticket journey
            else {
                //Add transaction type "value ticket" to string
                historyStr += " " + app_context.getResources().getString(R.string.ticket) + " - ";
                //If valu ticket is bouht for more than 1 person
                if (hist[i].getGroupSize() > 1) {
                    //Add number of ticket to the string
                    historyStr += "" + hist[i].getGroupSize() + " " + app_context.getResources().getString(R.string.pieces) + ", ";
                }

                //Add the price of the value ticket to the string, ending with euro-character
                historyStr +=
                         String.format("%d,%02d", (hist[i].getPrice() / 100), (hist[i].getPrice() % 100))
                        + "\u20ac"; //euro
            }

            //Add line break at the end if we've not reached the last history field
            if (i > 0)
                historyStr += "\n";
        }

        history = historyStr;
    }


    public void ETicketValidity(eTicket eTicket) {
        //Set date format to be used on output string
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("d.M.yyyy HH:mm", Locale.ENGLISH);

        //Instantiate helper class to get names for the ticket's zone or vehicle type where the ticket is valid
        ValidityAreaMappings mappings = new ValidityAreaMappings(app_context);
        //Get the tickets validity area name
        String validityArea = mappings.getValidityArea(eTicket.getValidityAreaType(), eTicket.getValidityArea());

        //Special handling of the case when ticket has no validity area set
        //We just assume it to mean the whole area (Region three-zone/Koko alue))
        if (validityArea.equalsIgnoreCase(app_context.getResources().getString(R.string.z0))) {
            validityArea = app_context.getResources().getString(R.string.z15);
        }

        //Ticket's validity start date
        Calendar periodStartCal = Calendar.getInstance();
        periodStartCal.setTime(eTicket.getValidityStartDate());
        //Ticket's validity end date
        Calendar periodEndCal = Calendar.getInstance();
        periodEndCal.setTime(eTicket.getValidityEndDate());

        //Current date from the device
        Calendar currentCal = Calendar.getInstance();

        //Ticket status string
        String status = app_context.getResources().getString(R.string.ticketStatus) + " ";
        //String to tell more about validity of the ticket
        String validityStr = "\n";

        //NOTE! If a date is not set on ticket, it's extracted date value is 1.1.1997!
        //This is due to date format on tickets which stores the number of days since 1.1.1997

        //If no end date is set for the ticket (the date is 1.1.1997)
        if (periodEndCal.get(Calendar.YEAR) == 1997) {
            //Ticket is unused
            status += app_context.getResources().getString(R.string.unused);
            //Tell user when the validity starts
            validityStr += app_context.getResources().getString(R.string.ticketValidity);
        }
        //If start date is set, but start date is in the future
        else if (periodStartCal.after(currentCal)) {
            //Ticket is not yet valid
            status += app_context.getResources().getString(R.string.notStarted);

            //tell start and end dates for the validity
            validityStr += app_context.getResources().getString(R.string.valid)
                    + datetimeFormat.format(periodStartCal.getTime()) + " - "
                    + datetimeFormat.format(periodEndCal.getTime());
        }
        //If ticket's validity end date was before current date
        else if (periodEndCal.before(currentCal)) {
            //Ticket is used and no longer valid
            status += app_context.getResources().getString(R.string.valid) + "";

            //Tell the time when the validity of the ticket will end or has ended
            validityStr += app_context.getResources().getString(R.string.validUntil) + datetimeFormat.format(periodEndCal.getTime());
        }
        //no other options left, ticket is valid
        else {
            //Valid ticket
            status += app_context.getResources().getString(R.string.valid) + "\n";

            //Tell the time when the validity of the ticket will end or has ended
            validityStr += app_context.getResources().getString(R.string.validUntil) + " " + datetimeFormat.format(periodEndCal.getTime());
        }

        //Get validity length number from single ticket field ValidityLength
        //(the meaning of this number is later checked from ValidityLengthType -field)
        int valueLen = eTicket.getValidityLength();

        String infoStr = app_context.getResources().getString(R.string.zone) + ": " + validityArea + "\n"
                + app_context.getResources().getString(R.string.groupSize) + " " + eTicket.getGroupSize() + "\n"
                + app_context.getResources().getString(R.string.validityTime) + " " + String.valueOf(valueLen) + " ";

        //Add appropriate validity time unit based on tickets validityLengthType
        if (eTicket.getValidityLengthType() == MINUTES) {
            infoStr += app_context.getResources().getString(R.string.min);
        } else if (eTicket.getValidityLengthType() == HOURS) {
            infoStr += app_context.getResources().getString(R.string.h);
        } else if (eTicket.getValidityLengthType() == ALLDAY) {
            infoStr += app_context.getResources().getString(R.string.days);
        } else if (eTicket.getValidityLengthType() == DAYS) {
            infoStr += app_context.getResources().getString(R.string.days);
        }


        String retString = status + infoStr + validityStr;
        ETicketValidity = retString;
    }


    public void InitOtherValues() {
        boardingVehicle = travelCard.getBoardingVehicle();
        boardingArea = travelCard.getBoardingArea();

        Date getBoardingDate = travelCard.getBoardingDate();
        SimpleDateFormat ft1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Locale.ENGLISH);
        String mBoardingDate = ft1.format(getBoardingDate);
        boardingDate = mBoardingDate;

        loadedPeriodLength = travelCard.getLoadedPeriodLength();
        loadedPeriodPrice = travelCard.getLoadedPeriodPrice();
        loadedPeriodProduct = travelCard.getLoadedPeriodProduct();

        Date getLoadingDate = travelCard.getLoadingDate();
        SimpleDateFormat ft2 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Locale.ENGLISH);
        String mLoadingDate = ft2.format(getLoadingDate);
        loadingDate = mLoadingDate;

        loadingDeviceNumber = travelCard.getLoadingDeviceNumber();
        loadingOrganizationID = travelCard.getLoadingOrganizationID();
        loadingTime = travelCard.getLoadingTime();
        boardingDirection = travelCard.getBoardingDirection();
        boardingLocationNum = travelCard.getBoardingLocationNum();
        boardingLocationNumType = travelCard.getBoardingLocationNumType();
        historyLength = travelCard.getHistoryLen();
    }

    public long daysLeftValid(String EndingDate) {

        long days;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        try {
            Date date = sdf.parse(EndingDate);
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(date);
            endDate.add(Calendar.DATE, 1);
            endDate.set(Calendar.HOUR, 4);
            endDate.set(Calendar.MINUTE, 20);
            Calendar startDate = Calendar.getInstance();
            long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis(); //result in millis
            Log.e("Matkakortti Lollipop", "diff:" + diff);
            days =  diff / (24 * 60 * 60 * 1000);
            Log.e("Matkakortti Lollipop", "days:" + days);
            return days;
        } catch (ParseException e) {
            Log.e("Matkakortti Lollipop", "Helpperi@daysLeftValid - ERROR");
            days = 0;
        }

        return days;
    }



    public String getCardNumber() {
        return cardNumber;
    }

    public String getPeriod1Date() {
        return period1Date;
    }

    public String getPeriod1Status() {
        return period1Status;
    }

    public String getPeriod1Zone() {
        return period1Zone;
    }

    public String getPeriod2Date() {
        return period2Date;
    }

    public String getPeriod2Status() {
        return period2Status;
    }

    public String getPeriod2Zone() {
        return period2Zone;
    }

    public String getCardValue() {
        return cardValue;
    }

    public String getLoadedValue() {
        return loadedValue;
    }

    public String getHistory() {
        return history;
    }

    public String getETicketValidity() {
        return ETicketValidity;
    }

    public String getBoardingDate() {
        return boardingDate;
    }

    public String getLoadingDate() {
        return loadingDate;
    }

    public short getBoardingVehicle() {
        return boardingVehicle;
    }

    public short getLoadedPeriodLength() {
        return loadedPeriodLength;
    }

    public short getLoadedPeriodProduct() {
        return loadedPeriodProduct;
    }

    public short getLoadingDeviceNumber() {
        return loadingDeviceNumber;
    }

    public short getLoadingOrganizationID() {
        return loadingOrganizationID;
    }

    public short getBoardingLocationNum() {
        return boardingLocationNum;
    }

    public byte getBoardingArea() {
        return boardingArea;
    }

    public byte getLoadingTime() {
        return loadingTime;
    }

    public byte getBoardingDirection() {
        return boardingDirection;
    }

    public byte getBoardingLocationNumType() {
        return boardingLocationNumType;
    }

    public byte getHistoryLength() {
        return historyLength;
    }

    public long getDaysLeftValid1() {
        return daysLeftValid1;
    }

    public long getDaysLeftValid2() {
        return daysLeftValid2;
    }

    public int getLoadedPeriodPrice() {
        return loadedPeriodPrice;
    }
}


