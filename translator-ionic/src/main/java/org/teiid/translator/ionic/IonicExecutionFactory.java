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

import javax.resource.cci.ConnectionFactory;

import org.teiid.core.types.DataTypeManager;
import org.teiid.language.QueryExpression;
import org.teiid.language.Select;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.RuntimeMetadata;
import org.teiid.metadata.Table;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.ResultSetExecution;
import org.teiid.translator.Translator;
import org.teiid.translator.TranslatorException;
import org.teiid.translator.TypeFacility;

@Translator(name="ionic", description="Ionic translator for enforcing entitlements")
public class IonicExecutionFactory extends ExecutionFactory<ConnectionFactory, IonicConnection> {
	public static final String CREATE_KEY_TAG = "create_key_tag";
	public static final String HAS_ACCESS = "has_col_access";
	public static final String KEY_TAG = "key_tag";
	public static final String PERMISSIONS_TABLE = "Permissions";
	private static final String IONIC = "ionic"; //$NON-NLS-1$
	
    @Override
    public void start() throws TranslatorException {
    	super.start();
    	//access = has_access(table_name, col_name, col_key_tag)
        addPushDownFunction(IONIC, HAS_ACCESS, TypeFacility.RUNTIME_NAMES.BOOLEAN, 
        		TypeFacility.RUNTIME_NAMES.STRING, TypeFacility.RUNTIME_NAMES.STRING, TypeFacility.RUNTIME_NAMES.STRING); //$NON-NLS-1$
        // keytag = create_key_tag(table_name, pk)
        addPushDownFunction(IONIC, CREATE_KEY_TAG, TypeFacility.RUNTIME_NAMES.STRING, 
        		TypeFacility.RUNTIME_NAMES.STRING, TypeFacility.RUNTIME_NAMES.OBJECT); //$NON-NLS-1$ 
    }

    @Override
	public ResultSetExecution createResultSetExecution(QueryExpression command,
			ExecutionContext executionContext, RuntimeMetadata metadata,
			IonicConnection connection)
    		throws TranslatorException {
    	return new IonicExecution((Select)command, executionContext, metadata, getConnection(null, executionContext));
    }    
    
    @Override
    public boolean supportsInCriteria() {
        return true;
    }

    @Override
    public boolean isSourceRequired() {
    	return false;
    }
	
    @Override
	public boolean isSourceRequiredForMetadata() {
		return false;
	}

	@Override
	public boolean isSourceRequiredForCapabilities() {
		return false;
	}     
    
	@Override
	public boolean supportsOnlyLiteralComparison() {
		return true;
	}    
	
	@Override
	public boolean supportsSelectWithoutFrom(){
		return true;
	}
	
	@Override
	public boolean supportsCompareCriteriaEquals(){
		return true;
	}
    
	@Override
	public void getMetadata(MetadataFactory metadataFactory,
			IonicConnection connection) throws TranslatorException {
		
        Table table = metadataFactory.addTable(PERMISSIONS_TABLE);
        table.setSupportsUpdate(false);
        
        metadataFactory.addColumn(KEY_TAG, DataTypeManager.DefaultDataTypes.STRING, table);
	}

	@Override
	public IonicConnection getConnection(ConnectionFactory factory,
			ExecutionContext executionContext) throws TranslatorException {
		return new IonicConnectionImpl();
	}
}
