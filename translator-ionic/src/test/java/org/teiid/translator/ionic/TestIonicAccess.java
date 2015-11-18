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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.resource.cci.ConnectionFactory;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.jboss.security.SimplePrincipal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.teiid.core.util.UnitTestUtil;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.security.Credentials;
import org.teiid.services.SessionServiceImpl;
import org.teiid.services.TeiidLoginContext;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.TranslatorException;
import org.teiid.translator.loopback.LoopbackExecutionFactory;

public class TestIonicAccess {

	private EmbeddedServer es;
	
	class SecurtityAwareEmbeddedServer extends EmbeddedServer {
		public SecurtityAwareEmbeddedServer(){
			this.sessionService = new SessionServiceImpl() {
				@Override
				protected TeiidLoginContext authenticate(String userName,
						Credentials credentials, String applicationName,
						String securityDomain) throws LoginException {
					if (userName.equals("sally") || userName.equals("joe") || userName.equals("mary")){
						Subject subject = new Subject();
						SimplePrincipal p = new SimplePrincipal(userName);
						subject.getPrincipals().add(p);
						return new TeiidLoginContext(userName + AT
								+ securityDomain, subject, securityDomain,
								this.securityHelper.getSecurityContext());					
					}
					throw new LoginException("Login failed");
				}
			};
		}
	}
	
	@Before 
	public void setup() throws Exception {
		es = new SecurtityAwareEmbeddedServer();
		EmbeddedConfiguration ec = new EmbeddedConfiguration();
		ec.setSecurityHelper(new MockSecurityHelper());
		es.start(ec);
		
		LoopbackExecutionFactory loopback = new LoopbackExecutionFactory();
		loopback.start();
		
		IonicExecutionFactory ionic = new IonicExecutionFactory(){
			@Override
			public IonicConnection getConnection(ConnectionFactory factory,
					ExecutionContext executionContext) throws TranslatorException {
				return new MockIonicConnection();
			}			
		};
		ionic.start();
		
		es.addTranslator("loopback", loopback);
		es.addTranslator("ionic", ionic);
		es.deployVDB(new FileInputStream(UnitTestUtil.getTestDataFile("ionic-vdb.xml")));
	}
	
	@After 
	public void teardown() {
		es.stop();
	}
	
	@Test
	public void checkRowTable() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", "sally");
		
		Connection conn = es.getDriver().connect("jdbc:teiid:ionic", props);
		ResultSet rs = conn.createStatement().executeQuery("select * from KT.Rowtable");
		rs.next();
		assertEquals(0, rs.getInt(1));
		assertEquals("tag1", rs.getString(2));
		conn.close();
	}
	
	@Test
	public void testRowFilter() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", "sally");
		testByUser(props, new Object[] {0, "ABCD"});
		
		props = new Properties();
		props.setProperty("user", "joe");
		testByUser(props, new Object[] {1, "EFGH"});
		
		props = new Properties();
		props.setProperty("user", "mary");
		testByUser(props, new Object[] {0, "ABCD", 1, "EFGH", 2, "IJKL"});
	}

	private void testByUser(Properties props, Object[] expected) throws SQLException {
		Connection conn = es.getDriver().connect("jdbc:teiid:ionic", props);
		int i = 0;
		ResultSet rs = conn.createStatement().executeQuery("select * from VM1.G1WithRowFilter");
		if (rs.next()){
			assertEquals(expected[i++], rs.getInt(1));
			assertEquals(expected[i++], rs.getString(2));
			while(rs.next()){
				assertEquals(expected[i++], rs.getInt(1));
				assertEquals(expected[i++], rs.getString(2));				
			}
		} else {
			fail("no rows");
		}
		conn.close();
	}
	
	@Test
	public void testColTable() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", "sally");
		
		Connection conn = es.getDriver().connect("jdbc:teiid:ionic", props);
		ResultSet rs = conn.createStatement().executeQuery("select ionic.has_col_access('PM1.G1','e1', 'tag1')");
		rs.next();
		assertEquals(true, rs.getBoolean(1));
		
		rs = conn.createStatement().executeQuery("select ionic.has_col_access('PM1.G1','e1', 'tag2')");
		rs.next();
		assertEquals(false, rs.getBoolean(1));

		conn.close();		
	}
	
	
	@Test
	public void testRowColFiltering() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", "sally");
		
		Connection conn = es.getDriver().connect("jdbc:teiid:ionic", props);
		ResultSet rs = conn.createStatement().executeQuery("select * from VM1.G1WithRowColFilter");
		rs.next();
		assertEquals(0, rs.getInt(1));
		assertEquals("ABCD", rs.getString(2));

		conn.close();		
	}	
}
