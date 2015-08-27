
package br.cefetrj.sagitarii.action;

import java.io.PrintWriter;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import br.cefetrj.sagitarii.misc.PathFinder;

@Action (value = "saveExecutorText", results = { @Result (type="redirect", location = "viewExecutors", name = "ok"),
		 @Result (type="redirect", location = "viewExecutors", name = "error") }, interceptorRefs= { @InterceptorRef("seguranca")	 } ) 

@ParentPackage("default")
public class SaveExecutorTextAction extends BasicActionClass {
	private String fileName;
	private String fileContent;
	
	public String execute () {
		
		try {
			String file = PathFinder.getInstance().getPath() + "/" + fileName;
			PrintWriter out = new PrintWriter( file );
			out.println( fileContent );
			out.close();
			
		} catch ( Exception e ) {
			setMessageText( "Error: " + e.getMessage() );
			return "error";
		}
		
		return "ok";
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


}
