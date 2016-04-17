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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.cefetrj.sagitarii.teapot.Configurator;

 
public class Client {
	private List<String> filesToSend;
	private String storageAddress;
	private int storagePort;
	private String sessionSerial;
	private String sagiHost;
	private int maxUploadThreads;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	public Client( Configurator configurator ) {
		filesToSend = new ArrayList<String>();
		this.storageAddress = configurator.getStorageHost();
		this.storagePort = configurator.getStoragePort();
		this.maxUploadThreads = configurator.getMaxUploadThreads();
		this.sagiHost = configurator.getHostURL();
	}
	
	public void sendFile( String fileName, String folder, String targetTable, String experimentSerial,  
			String macAddress) throws Exception {

		String instanceSerial = "";
		String activity = "";
		String fragment = "";
		String taskId = "";
		String exitCode = "0";
		String startTimeMillis = "";
		String finishTimeMillis = "";
		
		
		getSessionKey();
		
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		xml.append("<session macAddress=\""+macAddress+"\" instance=\""+instanceSerial+
				"\" activity=\""+activity+"\"  taskId=\""+taskId+"\" exitCode=\""+exitCode+"\" fragment=\""+fragment + 
				"\" startTime=\""+startTimeMillis + "\" finishTime=\""+finishTimeMillis +
				"\" totalFiles=\"#TOTAL_FILES#\" experiment=\""+experimentSerial + "\" id=\""+sessionSerial+"\" targetTable=\""+targetTable+"\">\n");
		
		
		File fil = new File(folder + "/" + fileName);
		if ( fil.exists() ) {
			xml.append("<file name=\""+fileName+"\" type=\"FILE_TYPE_CSV\" />\n");
			filesToSend.add( folder + "/" + fileName );
		} else {
			logger.error("will not send sagi_output.txt in session.xml file: this activity instance produced no data");
		}
		

		File filesFolder = new File( folder + "/" + "outbox" );
	    for (final File fileEntry : filesFolder.listFiles() ) {
	        if ( !fileEntry.isDirectory() ) {
	        	//ZipUtil.compress( folder + "/" + "outbox/" + fileEntry.getName(), folder + "/" + "outbox/" + fileEntry.getName() + ".gz" );
	    		xml.append("<file name=\""+fileEntry.getName()+"\" type=\"FILE_TYPE_FILE\" />\n");
	    		filesToSend.add( folder + "/outbox/" + fileEntry.getName() );
	        }
	    }
		
		
		File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath() );
		String storageRootFolder =  f.getAbsolutePath();
		storageRootFolder = storageRootFolder.substring(0, storageRootFolder.lastIndexOf( File.separator ) + 1) + "namespaces/";
		
		//String folderName = "outbox";
		String folderPath = folder.replace(storageRootFolder, "").replaceAll("/+", "/");
		
		logger.debug("sending content of folder:");
		logger.debug(" > " + folderPath );
		
	    xml.append("<file name=\"session.xml\" type=\"FILE_TYPE_SESSION\" />\n");
	    
	    xml.append("<console><![CDATA[");
	    xml.append("]]></console>");

	    
	    xml.append("<execLog><![CDATA[");

	    xml.append("]]></execLog>");
	    
		xml.append("</session>\n");
		filesToSend.add( folder + "/" + "session.xml" );
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folder + "/" + "session.xml"), "UTF-8"));
		writer.write( xml.toString().replace("#TOTAL_FILES#", String.valueOf(filesToSend.size()) ) );
		writer.close();

		// Send files
		if ( filesToSend.size() > 0 ) {
			logger.debug("need to send " + filesToSend.size() + " files to Sagitarii...");
			uploadFiles( filesToSend, targetTable, experimentSerial, sessionSerial, folderPath );
		}
		commit();
	}
	
	private void uploadFiles( List<String> fileNames, String targetTable, 
			String experimentSerial, String sessionSerial, String sourcePath ) throws Exception {

		logger.debug("starting Multithread Uploader for session " + sessionSerial + " with " + maxUploadThreads + " threads." );
		MultiThreadUpload mtu = new MultiThreadUpload( maxUploadThreads );
		mtu.upload(fileNames, storageAddress, storagePort, 
				targetTable, experimentSerial, sessionSerial, sourcePath);
		
	}	
	
	private void commit() throws Exception {
		URL url = new URL( sagiHost + "/sagitarii/transactionManager?command=commit&sessionSerial=" + sessionSerial );
		Scanner s = new Scanner( url.openStream() );
		String response = s.nextLine();
		logger.debug("session "+sessionSerial+" commit: " + response);
		s.close();
	}
	
	private void getSessionKey() throws Exception {
		URL url = new URL( sagiHost + "/sagitarii/transactionManager?command=beginTransaction");
		Scanner s = new Scanner( url.openStream() );
		sessionSerial = s.nextLine();
		logger.debug("open session " + sessionSerial );
		s.close();
	}
	
}