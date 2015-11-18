/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.translator.ionic;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.teiid.language.Select;
import org.teiid.metadata.RuntimeMetadata;
import org.teiid.translator.DataNotAvailableException;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.ResultSetExecution;
import org.teiid.translator.TranslatorException;


/**
 * Represents the execution of a command.
 */
public class IonicExecution implements ResultSetExecution {

	private Select command;
	private ExecutionContext executionContext;
	private RuntimeMetadata metadata;
	private IonicConnection connection;
	private Iterator<?> resultIter;
	
	public IonicExecution (Select command,
			ExecutionContext executionContext, RuntimeMetadata metadata,
			IonicConnection connection){
		this.command = command;
		this.executionContext = executionContext;
		this.metadata = metadata;
		this.connection = connection;
	}
	
	public void execute() throws TranslatorException {
		IonicSQLVisitor visitor = new IonicSQLVisitor();
		visitor.execute(this.command, this);
	}
	
	public void execute(String tableName, List<String> columns, Map<String, List<String>> values){
		if (tableName.equalsIgnoreCase(IonicExecutionFactory.PERMISSIONS_TABLE)){
			List<?> result = this.connection.filter(this.executionContext.getSubject(), tableName, 
					values.get(IonicExecutionFactory.KEY_TAG));
			if (result != null) {
				this.resultIter = result.iterator();
			}
		}
	}
	
	public void executeFunction(String functionName, List<String> params) {
		if (functionName.equals(IonicExecutionFactory.HAS_ACCESS)){
			boolean hasAccess = this.connection.hasColumnAccess(this.executionContext.getSubject(), params.get(0), params.get(1), params.get(2));
			ArrayList<Boolean> result = new ArrayList<Boolean>();
			result.add(hasAccess);
			this.resultIter = result.iterator();
		}
	}	

	public List<?> next() throws TranslatorException, DataNotAvailableException {
		if (this.resultIter != null && this.resultIter.hasNext()) {
			return Arrays.asList(this.resultIter.next());
		}
		this.resultIter = null;
		return null;
	}
	
	public void close() {
		this.resultIter = null;
	}

	public void cancel() throws TranslatorException {
		//no-op - can not stop
	}
}
