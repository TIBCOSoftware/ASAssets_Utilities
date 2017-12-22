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
	    Accepts SQL text (as returned by one of the RequestUtils) and a column name. 
	    The SQL text is parsed and scanned for any expressions like "column name" = 'val' 
	    or "column name" IN (val1, val2, ..valX). All the values found are returned in a cursor.
	
	Inputs:
	    sql -      - The SQL to search
	
	    columnName - The column to search for.
	
	Outputs:
	    result (   - Cursor containing the values of the where clause condition(s).
	        value  - A located value.
	    )
	
	Exceptions:
	    None
	
	Author:      Mike DeAngelo
	Date:        11/11/2011
	CSW Version: 6.0.0
	
*/
import com.compositesw.extension.*;
import java.sql.*;

import com.compositesw.cdms.services.parser.ParserImpl;
import com.compositesw.cdms.services.parser.ParserException;
import com.compositesw.cdms.services.parser.Command;
import com.compositesw.cdms.services.parser.CommandVisitor;
import com.compositesw.cdms.services.parser.Delete;
import com.compositesw.cdms.services.parser.Update;
import com.compositesw.cdms.services.parser.Select;
import com.compositesw.cdms.services.parser.Insert;
import com.compositesw.cdms.services.parser.NonJoinQueryExpression;
import com.compositesw.cdms.services.parser.WhereNode;
import com.compositesw.cdms.services.parser.WhereNodeVisitor;
import com.compositesw.cdms.services.parser.LeafWhereNode;
import com.compositesw.cdms.services.parser.InPredicate;
import com.compositesw.cdms.services.parser.LikePredicate;
import com.compositesw.cdms.services.parser.NullPredicate;
import com.compositesw.cdms.services.parser.ExistsPredicate;
import com.compositesw.cdms.services.parser.BetweenPredicate;
import com.compositesw.cdms.services.parser.BinaryWhereNode;
import com.compositesw.cdms.services.parser.Selectable;
import com.compositesw.cdms.services.parser.Literal;


public class ReadInEqClause
    implements CustomProcedure
{

    private ExecutionEnvironment qenv;

    private String[] results;
    private String columnName;

    public ReadInEqClause() {}

    /**
     * This is called just once after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
    public void initialize(ExecutionEnvironment qenv)
        throws CustomProcedureException
    {
        this.qenv = qenv;
        log(LOG_DEBUG,"ReadInEqClause.initialize called");
    }

    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    public ParameterInfo[] getParameterInfo()
    {
        log(LOG_DEBUG,"ReadInEqClause.getParameterInfo called");

        return new ParameterInfo[] {
            new ParameterInfo("sql", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("columnName", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("result", TYPED_CURSOR, DIRECTION_OUT,
                    new ParameterInfo[] {
                        new ParameterInfo("value", Types.VARCHAR),
                    }),
        };
    }

    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException
    {
        log(LOG_DEBUG,"ReadInEqClause.invoke called");
        String sql = (String)inputValues[0];
        columnName = (String)inputValues[1];

        Command cmd = null;
        try {
            cmd = ParserImpl.parseSql(sql);
        }
        catch (ParserException pe) {
            throw new CustomProcedureException(pe);
        }

        WhereClauseExtractor wce = new WhereClauseExtractor(); ;

        cmd.accept(wce);

        MyWhereNodeVisitor wnv = new MyWhereNodeVisitor();

        WhereNode wn = wce.getWhereNode();
        if (wn == null)
            return;
        wn.accept(wnv);

        results = wnv.getValues();
    }

    class WhereClauseExtractor extends CommandVisitor {
        public WhereClauseExtractor() {}
        private WhereNode whereNode;
        public void visitDelete(Delete delete){}
        public void visitUpdate(Update update){}
        public void visitSelect(Select select)
        {
            whereNode = select.getWherenode();
        }
        public void visitInsert(Insert insert){}
        public void visitNonJoinQueryExpression(NonJoinQueryExpression expression){}
        public String getSql()
        {
            return whereNode.getSql();
        }
        public WhereNode getWhereNode() {
            return whereNode;
        }
    }

    class MyWhereNodeVisitor extends WhereNodeVisitor {
        private Selectable[] values;
        public void visit(WhereNode node)
        {
            node.accept(this);
        }

        protected void visitLeafWhereNode(LeafWhereNode node) {
            if(!node.getOp().equals("="))
                return;

            Selectable left = node.getLeft();
            Selectable right = node.getRight();
            Selectable value = null;
            if((right instanceof Literal) && (left.getName().equals(columnName))) {
                value = right;
            }
            else if((left instanceof Literal) && (right.getName().equals(columnName))) {
                value = left;
            }
            else
                return;

            if (values == null)
                values = new Selectable[] {value};
            else {
                Selectable[] savedValues = this.values;
                this.values = new Selectable[values.length + 1];
                for (int i = 0; i < savedValues.length; i++) {
                    this.values[i] = savedValues[i];
                }
                this.values[savedValues.length] = value;
            }
        }

        protected void visitInPredicate(InPredicate inPredicate) {
            if (!inPredicate.getValue().getName().equals(columnName)) {
                return;
            }
            Selectable[] values = inPredicate.getValueList();
            if (this.values == null) {
                this.values = values;
            }
            else {
                Selectable[] savedValues = this.values;
                this.values = new Selectable[savedValues.length + values.length];
                for (int i = 0; i < savedValues.length; i++) {
                    this.values[i] = savedValues[i];
                }
                for (int i = 0; i < values.length; i++) {
                    this.values[savedValues.length + i] = values[i];
                }
            }
        }

        protected void visitLikePredicate(LikePredicate likePredicate) {}

        protected void visitNullPredicate(NullPredicate nullPredicate) {}

        protected void visitExistsPredicate(ExistsPredicate existsPredicate) {}

        protected void visitBinaryWhereNode(BinaryWhereNode bnode)
        {
            visit(bnode.getLeft());
            visit(bnode.getRight());
        }

        protected void visitBetweenPredicate(BetweenPredicate b) {
            visitBinaryWhereNode(b);
        }

        public String[] getValues() {
            if (values == null)
                return null;
            if (values.length == 0) {
                return null;
            }
            String[] list = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Literal) {
                    list[i] = ((Literal)values[i]).getIafValue().toString();
                }
                else
                    list[i] = values[i].toString();
            }
            return list;
        }
    }

    /**
     * Called to retrieve the number of rows that were inserted,
     * updated, or deleted during the execution of the procedure.  A
     * return value of -1 indicates that the number of affected rows is
     * unknown.  Can throw CustomProcedureException or SQLException if
     * there is an error when getting the number of affected rows.
     */
    public int getNumAffectedRows()
        throws CustomProcedureException, SQLException
    {
        log(LOG_DEBUG,"ReadInEqClause.getNumAffectedRows called");

        return -1;
    }

    /**
     * Called to retrieve the output values.  The returned objects
     * should obey the Java to SQL typing conventions as defined in the
     * table above.  Output cursors can be returned as either
     * CustomCursor or java.sql.ResultSet.  Can throw
     * CustomProcedureException or SQLException if there is an error
     * when getting the output values.  Should not return null.
     */
    public Object[] getOutputValues()
        throws CustomProcedureException, SQLException
    {
        log(LOG_DEBUG,"ReadInEqClause.getOutputValues called");

        return new Object[] {
            new CustomCursor() {
                int row = 0;
                public ParameterInfo[] getColumnInfo() {
                    return new ParameterInfo[] {
                        new ParameterInfo("value",Types.VARCHAR),
                    };
                }
                public boolean hasNext() {
                    if (results == null)
                        return false;
                    if (row < results.length) {
                        return true;
                    }
                    return false;
                }
                public Object[] next() {
                    if (results == null)
                        return null;
                    if (row < results.length) {
                        Object[] values =  new Object[] { results[row] };
                        row++;
                        return values;
                    }
                    return null;
                }
                public void close() {}
            }
        };
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close() throws SQLException, CustomProcedureException {
        log(LOG_DEBUG,"ReadInEqClause.close called");
    }

    //
    // Introspect methods
    //

    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    public String getName() {
        log(LOG_DEBUG,"ReadInEqClause.getName called");
        return "ReadInEqClause";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        log(LOG_DEBUG,"ReadInEqClause.getDescription called");
        return "ReadInEqClause";
    }

    //
    // Transaction methods
    //

    /**
     * Returns true if the custom procedure uses transactions.  If this method
     * returns false then commit and rollback will not be called.
     */
    public boolean canCommit() {
        log(LOG_DEBUG,"ReadInEqClause.canCommit called");
        return false;
    }

    /**
     * Commit any open transactions.
     */
    public void commit() throws CustomProcedureException , SQLException{
        log(LOG_DEBUG,"ReadInEqClause.commit called");
    }

    /**
     * Rollback any open transactions.
     */
    public void rollback() throws CustomProcedureException, SQLException {
        log(LOG_DEBUG,"ReadInEqClause.rollback called");
    }

    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        log(LOG_DEBUG,"ReadInEqClauseTest.canCompensate called");
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate(ExecutionEnvironment compQenv)
        throws CustomProcedureException , SQLException{
        this.qenv = compQenv;
        log(LOG_DEBUG,"ReadInEqClauseTest.compensate called");
    }

    private void log(int level, String msg) {
        if (qenv == null) {
            System.out.println(msg);
        }
        else {
            qenv.log(level, msg);
        }
    }

    public static void main(String[] args)
      throws Exception
    {
        CustomProcedure cp = new ReadInEqClause();
        cp.initialize(null);
        cp.invoke(args);
        Object outValue = cp.getOutputValues()[0];

        System.out.println("got " + outValue.toString());
        if (outValue instanceof byte[]) {
          byte[] bytes = (byte[]) outValue;
          for (byte b : bytes) {
              System.out.print(b);
          }
          System.out.print('\n');
        }
        cp.close();
    }
}
