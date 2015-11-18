package org.teiid.translator.ionic;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.teiid.resource.spi.BasicConnection;

public class MockIonicConnection extends BasicConnection implements IonicConnection {

	public List<String> filter(Subject subject, String sourceTableName,
			List<String> keyTags) {
		Set<Principal> users = subject.getPrincipals();
		if (!users.isEmpty()) {
			Principal p = users.iterator().next();
			if (p.getName().equals("sally")){
				return Arrays.asList("tag1");
			} else if (p.getName().equals("joe")){
				return Arrays.asList("tag2");
			} else if (p.getName().equals("mary")){
				return Arrays.asList("tag1","tag2","tag3");
			}
		}
		return null;
	}

	public boolean hasColumnAccess(Subject subject, String sourceTableName,
			String columnName, String keytag) {
		
		Set<Principal> users = subject.getPrincipals();
		if (!users.isEmpty()) {
			Principal p = users.iterator().next();
			if (p.getName().equals("sally") && keytag.equals("tag1")){
				return true;
			} else if (p.getName().equals("joe") && keytag.equals("tag2")){
				return true;
			} else if (p.getName().equals("mary")){
				return true;
			}			
		}		
		return false;
	}

	public void close() throws ResourceException {
		
	}
}
