package com.tibco.ps.utils.text;

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


/*
GenerateGuid

Description:
    Generates a random GUID value.


Inputs:
    N/A


Outputs:
    result - The GUID.
        Values: A GUID value.


Exceptions:
    N/A


Author:      Jatin Shah
Date:        10/11/2012
CSW Version: 5.2.0

*/

import java.util.UUID;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;
import com.compositesw.common.logging.Logger;

import java.sql.SQLException;
import java.sql.Types;

public class GenerateGuid implements CustomProcedure {

    private String result = "";
    private ExecutionEnvironment qEnv;

    private static Logger logger =
        Logger.getLogger("GenerateGuid");  

    
    public GenerateGuid() {}

    public void initialize(ExecutionEnvironment executionEnvironment) throws CustomProcedureException, SQLException {                                                                            
            if (logger.isDebug()) {
                logger.debug("GenerateGuid.initialize called");
            }
            qEnv = executionEnvironment;
    }

    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] {
            new ParameterInfo("result", Types.VARCHAR, DIRECTION_OUT)
        };

    }

    public void invoke(Object[] object) throws CustomProcedureException, SQLException {
        result = UUID.randomUUID().toString();
    }

    public String getName() {
        return "GenerateGuid";
    }

    public String getDescription() {
        return "Generates a random GUID value";
    }

    public boolean canCommit() {
        return false;
    }

    public void commit() throws CustomProcedureException, SQLException {
    }

    public void rollback() throws CustomProcedureException, SQLException {
    }

    public boolean canCompensate() {
        return false;
    }

    public void compensate(ExecutionEnvironment executionEnvironment) throws CustomProcedureException, SQLException {
    }

    public int getNumAffectedRows() throws CustomProcedureException, SQLException {
        return 0;
    }

    public Object[] getOutputValues() throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug("GenerateGuid.getOutputValues called");
        }
        return new Object[] { result };
    }

    public void close() throws CustomProcedureException, SQLException {
    }
}
