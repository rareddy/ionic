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

import java.security.Principal;

import javax.security.auth.Subject;

import org.teiid.security.SecurityHelper;

public class MockSecurityHelper implements SecurityHelper {

	private Object context;
	private Subject subject;
	
	public Object associateSecurityContext(Object context) {
		this.context = context;
		return context;
	}

	public void clearSecurityContext() {
		this.context = null;
	}

	public Object createSecurityContext(String arg0, Principal arg1,
			Object arg2, Subject arg3) {
		this.subject = arg3;
		return new Object();
	}

	public Object getSecurityContext() {
		return this.context;
	}

	public String getSecurityDomain(Object arg0) {
		return "teiid-security";
	}

	public Subject getSubjectInContext(String arg0) {
		return this.subject;
	}

	public boolean sameSubject(String arg0, Object arg1, Subject arg2) {
		return true;
	}
}