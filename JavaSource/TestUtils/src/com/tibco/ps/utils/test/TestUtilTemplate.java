package com.tibco.ps.utils.test;

/**
 * (c) 2017 TIBCO Software Inc. All rights reserved.
 * 
 * Except as specified below, this software is licensed pursuant to the Eclipse Public License v. 1.0.
 * The details can be found in the file LICENSE.
 * 
 * The following proprietary files are included as a convenience, and may not be used except pursuant
 * to valid license to Composite Information Server or TIBCO(R) Data Virtualization Server:
 * csadmin-XXXX.jar, csarchive-XXXX.jar, csbase-XXXX.jar, csclient-XXXX.jar, cscommon-XXXX.jar,
 * csext-XXXX.jar, csjdbc-XXXX.jar, csserverutil-XXXX.jar, csserver-XXXX.jar, cswebapi-XXXX.jar,
 * and customproc-XXXX.jar (where -XXXX is an optional version number).  Any included third party files
 * are licensed under the terms contained in their own accompanying LICENSE files, generally named .LICENSE.txt.
 * 
 * This software is licensed AS-IS. Support for this software is not covered by standard maintenance agreements with TIBCO.
 * If you would like to obtain assistance with this software, such assistance may be obtained through a separate paid consulting
 * agreement with TIBCO.
 * 
 */

/**
 * TestUtilTemplate.
 * Most of the TestUtil classes won't require any of the transacation
 * control methods, so we'll stub them out here and keep the actual
 * code cleaner. 
 */

import com.compositesw.extension.*;
import com.compositesw.common.logging.Logger;

import java.sql.*;

public abstract class TestUtilTemplate {

    protected static String className;
    protected static Logger logger;
    protected ExecutionEnvironment qenv;

    public TestUtilTemplate() {}

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
}
