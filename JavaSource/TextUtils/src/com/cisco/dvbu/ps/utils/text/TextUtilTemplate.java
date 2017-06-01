package com.cisco.dvbu.ps.utils.text;
/*
 * (c) 2005, 2014 Cisco and/or its affiliates. All rights reserved.

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

/**
 * TextUtilTemplate.
 * Most of the TextUtil classes won't require any of the transacation
 * control methods, so we'll stub them out here and keep the actual
 * code cleaner. 
 */
import com.compositesw.extension.*;
import com.compositesw.common.logging.Logger;

import java.sql.*;

public abstract class TextUtilTemplate
{
    protected static String className;
    protected static Logger logger;
    protected ExecutionEnvironment qenv;

    public TextUtilTemplate() {}

    public void initialize(ExecutionEnvironment qenv) {
        if (logger.isDebug()) {
            logger.debug(className + ".initialize called");
        }

        this.qenv = qenv;
    }

    public abstract ParameterInfo[] getParameterInfo();

    public abstract void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException;

    public int getNumAffectedRows() {
        if (logger.isDebug()) {
            logger.debug(className + ".getNumAffectedRows called");
        }

        return 0;
    }

    public abstract Object[] getOutputValues();

    public void close() throws SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".close called");
        }

    }

    public String getName() {
        if (logger.isDebug()) {
            logger.debug(className + ".getName called");
        }
        return className;
    }

    public abstract String getDescription();

    public boolean canCommit() {
        if (logger.isDebug()) {
            logger.debug(className + ".canCommit called");
        }
        return false;
    }

    public void commit() throws CustomProcedureException {
        if (logger.isDebug()) {
            logger.debug(className + ".commit called");
        }
    }

    public void rollback() throws CustomProcedureException {
        if (logger.isDebug()) {
            logger.debug(className + ".rollback called");
        }
    }

    public boolean canCompensate() {
        if (logger.isDebug()) {
            logger.debug(className + ".canCompensate called");
        }
        return false;
    }

    public void compensate(ExecutionEnvironment compQenv)
        throws CustomProcedureException {
        if (logger.isDebug()) {
            logger.debug(className + ".compensate called");
        }
    }
    
    public String stackTraceToString (Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append (element.toString());
            sb.append ("\n");
        }
        return sb.toString();
    }

}
