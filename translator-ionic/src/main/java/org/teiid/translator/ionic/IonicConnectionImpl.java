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

import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.security.auth.Subject;

public class IonicConnectionImpl implements IonicConnection {

	public Interaction createInteraction() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public LocalTransaction getLocalTransaction() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public ConnectionMetaData getMetaData() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSetInfo getResultSetInfo() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws ResourceException {
		// TODO Auto-generated method stub

	}

	public List<String> filter(Subject subject, String sourceTableName,
			List<String> keyTags) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasColumnAccess(Subject subject, String sourceTableName,
			String columnName, String keytag) {
		// TODO Auto-generated method stub
		return false;
	}

}
