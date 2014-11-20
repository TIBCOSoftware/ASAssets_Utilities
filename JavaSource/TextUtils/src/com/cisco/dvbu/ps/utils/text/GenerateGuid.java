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
    
    (c) 2012, 2014 Cisco and/or its affiliates. All rights reserved.

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
package com.cisco.dvbu.ps.utils.text;

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
