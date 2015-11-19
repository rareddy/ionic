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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teiid.language.Argument;
import org.teiid.language.Call;
import org.teiid.metadata.RuntimeMetadata;
import org.teiid.translator.DataNotAvailableException;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.ProcedureExecution;
import org.teiid.translator.TranslatorException;

public class IonicProcedureExecution implements ProcedureExecution {

	private Call command;
	private ExecutionContext executionContext;
	private RuntimeMetadata metadata;
	private IonicConnection connection;
	private Object result;
	
	public IonicProcedureExecution(Call command,
			ExecutionContext executionContext, RuntimeMetadata metadata,
			IonicConnection connection) {
		this.command = command;
		this.executionContext = executionContext;
		this.metadata = metadata;
		this.connection = connection;		
	}


	public void execute() throws TranslatorException {
		Map<String, Object> parameters = getParameters(this.command.getArguments());
		if (this.command.getProcedureName().equals(IonicExecutionFactory.HAS_COLUMN_ACCESS)){
			this.result = this.connection.hasColumnAccess(this.executionContext.getSubject(), 
					(String)parameters.get(IonicExecutionFactory.SOURCE_TABLE), 
					(String)parameters.get(IonicExecutionFactory.COLUMN_NAME), 
					(String)parameters.get(IonicExecutionFactory.KEY_TAG));
		} else if (command.getProcedureName().equals(IonicExecutionFactory.CREATE_KEY_TAG)) {
			this.result = this.connection.createKeyTag(this.executionContext.getSubject(), 
					(String)parameters.get(IonicExecutionFactory.SOURCE_TABLE), 
					(String)parameters.get(IonicExecutionFactory.PKEY));
			
		} else {
			throw new TranslatorException("Procedure not found");
		}
	}
	
	private Map<String, Object> getParameters(List<Argument> arguments){
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		for (Argument arg:arguments) {
			parameters.put(arg.getMetadataObject().getName(), arg.getArgumentValue().getValue());
		}
		return parameters;
	}
	
	public List<?> next() throws TranslatorException, DataNotAvailableException {
		return null;
	}

	public void close() {
	}	

	public void cancel() throws TranslatorException {
	}
	
	public List<?> getOutputParameterValues() throws TranslatorException {
		return Arrays.asList(this.result);
	}

}
