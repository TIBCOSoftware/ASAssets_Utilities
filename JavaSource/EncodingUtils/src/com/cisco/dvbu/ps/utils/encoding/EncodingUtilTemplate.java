package com.cisco.dvbu.ps.utils.encoding;

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.
 */
import java.sql.SQLException;
import java.util.Arrays;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;

/**
 * Base Custom Java Procedure (CJP) defining most of the methods required by CustomProcedure interface.
 * 
 * @see com.compositesw.extension.CustomProcedure
 * 
 * @author Alex Dedov, Composite Software
 * @date August, 2012
 * 
 * (c) 2012, 2014 Cisco and/or its affiliates. All rights reserved.
 */

public abstract class EncodingUtilTemplate implements CustomProcedure {

	protected ExecutionEnvironment cjpenv;
	protected String procName ;

	public EncodingUtilTemplate() {
	}

	/**
	 * Required by CustomProcedure interface.
	 * 
	 * @see
	 * com.compositesw.extension.CustomProcedure#initialize(com.compositesw.
	 * extension.ExecutionEnvironment)
	 */
	public void initialize(ExecutionEnvironment cjpEnv) {
		this.cjpenv = cjpEnv;
	}

	/**
	 * Required by CustomProcedure interface.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#close()
	 */
	public void close() {
	}

	/**
	 * Required by CustomProcedure interface. Used during introspection by
	 * Composite.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#getName()
	 */
	public abstract String getName() ; 

	/**
	 * Required by CustomProcedure interface. Used during introspection by
	 * Composite.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#getDescription()
	 */
	public abstract String getDescription() ;

	/**
	 * Required by CustomProcedure interface. Used during introspection by
	 * Composite. Allows to provide ResultSet metadata to the client applications.
	 * You may want to put your parameter description in there.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#getParameterInfo()
	 */
	public ParameterInfo[] getParameterInfo() {
		return new ParameterInfo[] {
//					new ParameterInfo("parameter1", Types.VARCHAR, DIRECTION_IN),
//					new ParameterInfo("parameter2", Types.INTEGER, DIRECTION_IN),
//					// ...etc...
//					new ParameterInfo("retcode", Types.INTEGER, DIRECTION_OUT),
//					new ParameterInfo("results", TYPED_CURSOR, DIRECTION_OUT,
//						new ParameterInfo[] {
//							new ParameterInfo("column1", Types.VARCHAR, DIRECTION_NONE),
//							new ParameterInfo("column2", Types.INTEGER, DIRECTION_NONE)
//						}
//					)
		} ;
	}

	/**
	 * This method is the CJP invocation mechanism. Required by CustomProcedure
	 * interface. 
	 * 
	 * @see com.compositesw.extension.CustomProcedure#invoke(java.lang.Object[])
	 */
	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException {
		try {
			log(LOG_DEBUG, "(p)PearsonBaseCJP.invoke started with parameters " + Arrays.asList(inputValues).toString() + "") ;
			int rc = execute( inputValues ) ;
			log(LOG_DEBUG, "(p)PearsonBaseCJP.invoke execution completed with ret. code=" + rc ) ;
		} 
		catch (Throwable t) {
			log(LOG_ERROR, "(p)PearsonBaseCJP.invoke failed " + t);
			if ( t instanceof CustomProcedureException )
				throw (CustomProcedureException)t ;
			else
				throw new CustomProcedureException("PearsonBaseCJP.invoke", t);
		}
	}

	
	public abstract int execute(Object[] args) throws Exception ;
	
	
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
		return new Object[] {};
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#getNumAffectedRows()
	 */
	public int getNumAffectedRows() {
		return -1;
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#canCommit()
	 */
	public boolean canCommit() {
		return false;
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#canCompensate()
	 */
	public boolean canCompensate() {
		return false;
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#commit()
	 */
	public void commit() {
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see com.compositesw.extension.CustomProcedure#rollback()
	 */
	public void rollback() {
	}

	/**
	 * Required by CustomProcedure interface. Not applicable in our case.
	 * 
	 * @see
	 * com.compositesw.extension.CustomProcedure#compensate(com.compositesw.
	 * extension.ExecutionEnvironment)
	 */
	public void compensate(ExecutionEnvironment qenv) {
	}

	/**
	 * Invokes writing internal (informational) messages via Composite's Environment
	 * 
	 * @param level
	 * @param msg
	 */
	protected void log(int level, String msg) {
		if (cjpenv == null) {
			System.out.println(msg);
		} else {
			cjpenv.log(level, msg);
		}
	}
	
	protected int getErrorCode(Throwable t) {
		// TODO: implement exception - error code mapping
		return -1 ;
	}

}
