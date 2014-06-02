package com.cisco.dvbu.ps.utils.xml;
/*
 * (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
 */

/**
 * XMLUtilTemplate.
 * Most of the XMLUtil classes won't require any of the transacation
 * control methods, so we'll stub them out here and keep the actual
 * code cleaner. 
 */
import com.compositesw.extension.*;
import com.compositesw.common.logging.Logger;

import java.sql.*;

public abstract class XMLUtilTemplate
{
    protected static String className;
    protected static Logger logger;
    protected ExecutionEnvironment qenv;

    public XMLUtilTemplate() {}

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
