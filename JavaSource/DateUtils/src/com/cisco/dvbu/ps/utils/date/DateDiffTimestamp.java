package com.cisco.dvbu.ps.utils.date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.Minutes;
import org.joda.time.Hours;
import org.joda.time.Days;
import org.joda.time.Weeks;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.Interval;
import java.util.Calendar;
import com.compositesw.extension.*;
import java.sql.*;

/*
	Description:
	  Calculates the difference of two TIMESTAMP values. It is leap year aware.
	
	
	Inputs:
	  datePart   - Unit of measure for the output "dateLength".
	    values: 'nanosecond', 'microsecond', 'millisecond', 'second', 'minute', 'hour', 'day', 'week', 'month' and 'year' (not case sensitive)
	
	  startDate  - The starting timestamp.
	    values: Any valid timestamp value.
	
	  endDate    - The ending timestamp.
	    values: Any valid timestamp value.
	
	
	Outputs:
	  dateLength - The number of "datePart"s between the beginning and end timestamps.
	    values: An integer. 0 if any of the inputs are invalid. Negative if the end timestamp
	            occurs before the start timestamp.
	
	
	Exceptions:
	  None
	
	
	Author:      Kevin O'Brien
	Date:        6/20/2010
	CSW Version: 5.1.0
	
	Updated By:  Alex Dedov 
	Date:        8/19/2011
	CSW Version: 5.1.0
	Reason:      Updated to support fractional seconds (to the nano-second.)
	
	(c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.

    This software is released under the Eclipse Public License. The details can be found in the file LICENSE. 
    Any dependent libraries supplied by third parties are provided under their own open source licenses as 
    described in their own LICENSE files, generally named .LICENSE.txt. The libraries supplied by Cisco as 
    part of the Composite Information Server/Cisco Data Virtualization Server, particularly csadmin-XXXX.jar, 
    csarchive-XXXX.jar, csbase-XXXX.jar, csclient-XXXX.jar, cscommon-XXXX.jar, csext-XXXX.jar, csjdbc-XXXX.jar, 
    csserverutil-XXXX.jar, csserver-XXXX.jar, cswebapi-XXXX.jar, and customproc-XXXX.jar (where -XXXX is an 
    optional version number) are provided as a convenience, but are covered under the licensing for the 
    Composite Information Server/Cisco Data Virtualization Server. They cannot be used in any way except 
    through a valid license for that product.

    This software is released AS-IS!. Support for this software is not covered by standard maintenance agreements with Cisco. 
    Any support for this software by Cisco would be covered by paid consulting agreements, and would be billable work.

*/

public class DateDiffTimestamp implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private Object result;


    /**
     * This is called once just after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
     public void initialize(ExecutionEnvironment qenv) throws SQLException
     {
         this.qenv = qenv;
     }


    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    public ParameterInfo[] getParameterInfo() {

        return new ParameterInfo[] {
            new ParameterInfo("datePart", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("startTimestamp", Types.TIMESTAMP, DIRECTION_IN),
            new ParameterInfo("endTimestamp", Types.TIMESTAMP, DIRECTION_IN),
            new ParameterInfo("dateLength", Types.BIGINT, DIRECTION_OUT)
        };
    }


    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException {
		Timestamp startTimestamp = null;
		Timestamp endTimestamp = null;
		Calendar startCal = null;
		Calendar endCal = null;
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		String datePart = null;
		long dateLength = 0;

		try {
			result = null;
			if (inputValues[0] == null) {
				result = new Long(dateLength);
				return;
			}

			if (inputValues[1] == null) {
				result = new Long(dateLength);
				return;
			}

			if (inputValues[2] == null) {
				result = new Long(dateLength);
				return;
			}

			datePart = (String) inputValues[0];
			startTimestamp = (Timestamp) inputValues[1];
			// long startMilliseconds = startTimestamp.getTime() +
			// (startTimestamp.getNanos() / 1000000);
			long startMilliseconds = startTimestamp.getTime()
					+ (startTimestamp.getNanos() % 1000000L >= 500000L ? 1 : 0);
			startCal = Calendar.getInstance();
			startCal.setTimeInMillis(startMilliseconds);

			endTimestamp = (Timestamp) inputValues[2];
			// long endMilliseconds = endTimestamp.getTime() +
			// (endTimestamp.getNanos() / 1000000);
			long endMilliseconds = endTimestamp.getTime()
					+ (endTimestamp.getNanos() % 1000000L >= 500000L ? 1 : 0);

			endCal = Calendar.getInstance();
			endCal.setTimeInMillis(endMilliseconds);

			startDateTime = new DateTime(startCal.get(Calendar.YEAR), 
                                   startCal.get(Calendar.MONTH) + 1, 
                                   startCal.get(Calendar.DAY_OF_MONTH), 
                                   startCal.get(Calendar.HOUR_OF_DAY), 
                                   startCal.get(Calendar.MINUTE),
					                         startCal.get(Calendar.SECOND), 
                                   startCal.get(Calendar.MILLISECOND)
                                  );
			endDateTime = new DateTime(endCal.get(Calendar.YEAR), 
                                 endCal.get(Calendar.MONTH) + 1,
                                 endCal.get(Calendar.DAY_OF_MONTH), 
                                 endCal.get(Calendar.HOUR_OF_DAY), 
                                 endCal.get(Calendar.MINUTE), 
                                 endCal.get(Calendar.SECOND),
                                 endCal.get(Calendar.MILLISECOND)
                                );

			Interval interval = new Interval(startDateTime, endDateTime);

			if (datePart.equalsIgnoreCase("second") || datePart.equalsIgnoreCase("ss")) {
				Seconds seconds = Seconds.secondsIn(interval);
				dateLength = seconds.getSeconds();
			} else if (datePart.equalsIgnoreCase("minute") || datePart.equalsIgnoreCase("mi")) {
				Minutes minutes = Minutes.minutesIn(interval);
				dateLength = minutes.getMinutes();
			} else if (datePart.equalsIgnoreCase("hour") || datePart.equalsIgnoreCase("hh")) {
				Hours hours = Hours.hoursIn(interval);
				dateLength = hours.getHours();
			} else if (datePart.equalsIgnoreCase("day") || datePart.equalsIgnoreCase("dd")) {
				Days days = Days.daysIn(interval);
				dateLength = days.getDays();
			} else if (datePart.equalsIgnoreCase("week") || datePart.equalsIgnoreCase("wk")) {
				Weeks weeks = Weeks.weeksIn(interval);
				dateLength = weeks.getWeeks();
			} else if (datePart.equalsIgnoreCase("month") || datePart.equalsIgnoreCase("mm")) {
				Months months = Months.monthsIn(interval);
				dateLength = months.getMonths();
			} else if (datePart.equalsIgnoreCase("year") || datePart.equalsIgnoreCase("yy")) {
				Years years = Years.yearsIn(interval);
				dateLength = years.getYears();
			} else if (datePart.equalsIgnoreCase("millisecond") || datePart.equalsIgnoreCase("ms")) {
				dateLength = 
					(endTimestamp.getTime() - startTimestamp.getTime()) ;			// millis
			} else if (datePart.equalsIgnoreCase("microsecond") || datePart.equalsIgnoreCase("mcs")) {
				dateLength = 
					((endTimestamp.getTime() - startTimestamp.getTime())/1000)		// seconds
            * 1000000L 															// micros
          + (endTimestamp.getNanos() - startTimestamp.getNanos())/1000 ; 		// nanos/1000
			} else if (datePart.equalsIgnoreCase("nanosecond") || datePart.equalsIgnoreCase("ns")) {
				dateLength = 
					((endTimestamp.getTime() - startTimestamp.getTime())/1000)		// seconds
            * 1000000000L 														// nanos
          + (endTimestamp.getNanos() - startTimestamp.getNanos()) ;			// nanos
			} 
			else {
				throw new IllegalArgumentException(datePart);
			}

			result = new Long(dateLength);

		} catch (Throwable t) {
			throw new CustomProcedureException(t);
		}
	}


    /**
     * Called to retrieve the number of rows that were inserted,
     * updated, or deleted during the execution of the procedure. A
     * return value of -1 indicates that the number of affected rows is
     * unknown.  Can throw CustomProcedureException or SQLException if
     * there is an error when getting the number of affected rows.
     */
    public int getNumAffectedRows() {
        return 0;
    }

    /**
     * Called to retrieve the output values.  The returned objects
     * should obey the Java to SQL typing conventions as defined in the
     * table above.  Output cursors can be returned as either
     * CustomCursor or java.sql.ResultSet.  Can throw
     * CustomProcedureException or SQLException if there is an error
     * when getting the output values.  Should not return null.
     */
    public Object[] getOutputValues() {

        Object[] outputValues = new Object[1];

        outputValues[0] = result;

        return outputValues;
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close()
        throws SQLException
    {
    }

    //
    // Introspection methods
    //

    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    public String getName() {
        return "DateDiffTimestamp";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "DateDiffTimestamp";
    }

    //
    // Transaction methods
    //

    /**
     * Commit any open transactions.
     */
    public void commit()
        throws SQLException
    { }

    /**
     * Rollback any open transactions.
     */
    public void rollback()
        throws SQLException
    { }

    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        return false;
    }

    public boolean canCommit(){
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate(ExecutionEnvironment qenv)
        throws CustomProcedureException, SQLException {
    }



    public static void main(String[] args)
    {
    }

}