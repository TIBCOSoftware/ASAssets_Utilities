package com.cisco.dvbu.ps.utils.request;

/*
	Description:
	    Walks the stack of requests that resulted in the call to this CJP and
	    returns the SQL of the original client request (or highest level request
	    that generated an SQL statement, e.g. if the original request is a procedure, 
	    then TopSqlRequest will return the first SQL request in the chain.) Note 
	    that this CJP does not return code for procedure requests.
	
	Inputs:
	    N/A
	
	Outputs:
	    requestSrc - SQL source code of the request.
	
	Exceptions:
	    None
	
	Author:      Mike DeAngelo
	Date:        11/11/2011
	CSW Version: 6.0.0
	
	(c) 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/
import com.compositesw.extension.*;
import com.compositesw.server.request.*;
import com.compositesw.server.customproc.ExecutionEnvironmentImpl;
import java.sql.*;
import java.util.Stack;

public class TopSqlRequest implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private String requestSrc;

    public TopSqlRequest() {} 

    public void initialize(ExecutionEnvironment qenv) {
        this.qenv = qenv;
    }

    public void close() {}

    public String getName() {
        return "TopSqlRequest";
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
        log(LOG_DEBUG,"TopSqlRequest.invoke called");

        Stack<Request> reqStack = new Stack<Request>();
        Request req = ((ExecutionEnvironmentImpl)qenv).getParentRequest();

        if (req == null) {
            return;
        }
        reqStack.push(req);

        while (req.getParentRequest() != null) {
            req = req.getParentRequest();
            reqStack.push(req);
        }

        while (!reqStack.empty()) {
            req = reqStack.pop();
            if (req instanceof SqlRequest) {
                requestSrc = req.getSourceCode();
                return;
            }
        }

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
