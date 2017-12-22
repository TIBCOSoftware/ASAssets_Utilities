package com.tibco.ps.utils.request;
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
	Description:
	    Walks the stack of requests that resulted in the call to this CJP and
	    returns the original client request (SQL statement or procedure call.)
	
	Inputs:
	    N/A
	
	Outputs:
	    requestSrc - SQL source code of the request.
	
	Exceptions:
	    None
	
	Author:      Mike DeAngelo
	Date:        11/11/2011
	CSW Version: 6.0.0
	
*/
import com.compositesw.extension.*;
import com.compositesw.server.request.*;
import com.compositesw.server.customproc.ExecutionEnvironmentImpl;
import java.sql.*;

public class OriginalRequest implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private String requestSrc;

    public OriginalRequest() {} 

    public void initialize(ExecutionEnvironment qenv) {
        this.qenv = qenv;
    }

    public void close() {}

    public String getName() {
        return "OriginalRequest";
    }

    public String getDescription() {
        return "Lookup current request path";
    }

    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] {
            new ParameterInfo("requestSrc",Types.VARCHAR,DIRECTION_OUT),
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        log(LOG_DEBUG,"OriginalRequest.invoke called");

        Request req = ((ExecutionEnvironmentImpl)qenv).getParentRequest();

        if (req == null) {
            return;
        }

        while (req.getParentRequest() != null) {
            req = req.getParentRequest();
        }

        if (req instanceof AbstractProcedureRequest) {
            Object path = ((AbstractProcedureRequest)req).getProcedurePath();
            if (path != null) {
                requestSrc = path.toString();
                return;
            }
        }
        requestSrc = req.getSourceCode();

    }

    public Object[] getOutputValues() {
        return new Object[] { requestSrc };
    }

    public int getNumAffectedRows() {
        return -1;
    }

    public boolean canCommit() {
        return false;
    }

    public boolean canCompensate() {
        return false;
    }

    public void commit() {}

    public void rollback() {}

    public void compensate(ExecutionEnvironment qenv) {}

    private void log(int level, String msg) {
        if (qenv == null) {
            System.out.println(msg);
        }
        else {
            qenv.log(level, msg);
        }
    }
}
