package br.cefetrj.sagitarii.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import br.cefetrj.sagitarii.core.NodesManager;

@Action (value = "clearNodeTasks", results = { @Result (type="redirect", location = "viewClusters", name = "ok") 
}, interceptorRefs= { @InterceptorRef("seguranca")	 } ) 

@ParentPackage("default")
public class ClearNodeTasksAction extends BasicActionClass {
	private String macAddress;
	
	public String execute(){

		try {
			NodesManager.getInstance().clearNodeTasks( macAddress );
		} catch ( Exception e ) {
			setMessageText( e.getMessage() );
		} 
			
		return "ok";
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
}
