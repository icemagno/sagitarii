
package br.cefetrj.sagitarii.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import br.cefetrj.sagitarii.persistence.exceptions.DatabaseConnectException;
import br.cefetrj.sagitarii.persistence.services.UserService;

@Action (value = "doRequestAccess", results = { @Result (type="redirect", location = "index", name = "ok")
} ) 

@ParentPackage("default")
public class DoRequestAccessAction extends BasicActionClass {
	private String username;
	private String password;
	private String fullName;
	private String mailAddress;
	private String details;
	
	public String execute () {
		
		try {
			
			UserService es = new UserService();
			es.requestAccess(fullName, username, password, mailAddress, details );
			setMessageText("Request sent");
			
		} catch ( DatabaseConnectException e ) {
			setMessageText( e.getMessage() );
		} catch ( Exception e ) {
			setMessageText("Error: " + e.getMessage() );
		} 
		
		return "ok";
	}


	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}

}
