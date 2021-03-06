package br.cefetrj.sagitarii.teapot.comm;
/**
 * Copyright 2015 Carlos Magno Abreu
 * magno.mabreu@gmail.com 
 *
 * Licensed under the Apache  License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required  by  applicable law or agreed to in  writing,  software
 * distributed   under the  License is  distributed  on  an  "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the  specific language  governing  permissions  and
 * limitations under the License.
 * 
 */

import br.cefetrj.sagitarii.teapot.Activation;
import br.cefetrj.sagitarii.teapot.Configurator;
import br.cefetrj.sagitarii.teapot.LogManager;
import br.cefetrj.sagitarii.teapot.Logger;
import br.cefetrj.sagitarii.teapot.SystemProperties;
import br.cefetrj.sagitarii.teapot.Task;

public class Uploader {
	private Configurator gf; 
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	public Uploader( Configurator gf  ) {
		this.gf = gf;
	}
	
	public void uploadCSV(String fileName, Activation act, Task task, SystemProperties tm) throws Exception {

		String relationName = act.getTargetTable();
		String experimentSerial = act.getExperiment(); 
		String filesFolderName = act.getNamespace();
		String macAddress = tm.getMacAddress();
		
		logger.debug( "uploading " + fileName + " to " + relationName + " for experiment " + experimentSerial );
		
		Client client = new Client( gf );
		client.sendFile( fileName, filesFolderName,	relationName, experimentSerial, macAddress, task );

		logger.debug( "done uploading " + fileName);
	}

}