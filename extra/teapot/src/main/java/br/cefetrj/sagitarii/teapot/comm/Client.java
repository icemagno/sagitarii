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

import br.cefetrj.sagitarii.teapot.Configurator;
import br.cefetrj.sagitarii.teapot.LogManager;
import br.cefetrj.sagitarii.teapot.Logger;
import br.cefetrj.sagitarii.teapot.Task;
 
public class Client {

	private String storageAddress;
	private int storagePort;
	private String sessionSerial;
	private String sagiHost;
	private int maxUploadThreads;
	private String hadoopConfigPath;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	public Client( Configurator configurator ) {
		this.hadoopConfigPath = configurator.getHadoopConfigPath();
		this.storageAddress = configurator.getStorageHost();
		this.storagePort = configurator.getStoragePort();
		this.maxUploadThreads = configurator.getMaxUploadThreads();
		this.sagiHost = configurator.getHostURL();
	}
	
	
	public void sendFile( String fileName, String folder, String targetTable, String experimentSerial,  
			String macAddress, Task task ) throws Exception {

		List<String> dataFilesList = new ArrayList<String>();
		List<String> controlFilesList = new ArrayList<String>();
		
		String instanceSerial = "";
		String activity = "";
		String fragment = "";
		String taskId = "";
		String exitCode = "0";
		String startTimeMillis = "";
		String finishTimeMillis = "";
		String cpuCost = "0";
		
		if ( task != null ) {
			instanceSerial = task.getActivation().getInstanceSerial();
			activity = task.getActivation().getActivitySerial();
			fragment = task.getActivation().getFragment();
			exitCode = String.valueOf( task.getExitCode() );
			taskId = task.getActivation().getTaskId();
			
			startTimeMillis = String.valueOf( task.getRealStartTime().getTime() );
			finishTimeMillis = String.valueOf( task.getRealFinishTime().getTime() );
			cpuCost = String.valueOf( task.getCpuCost() );
			
		}			
		
		getSessionKey( macAddress );
		
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		xml.append("<session macAddress=\""+macAddress+"\" instance=\""+instanceSerial+
				"\" activity=\""+activity+"\"  taskId=\""+taskId+"\" exitCode=\""+exitCode+"\" fragment=\""+fragment + 
				"\" startTime=\""+startTimeMillis + "\" finishTime=\""+finishTimeMillis +
				"\" cpuCost=\""+cpuCost +
				"\" totalFiles=\"#TOTAL_FILES#\" experiment=\""+experimentSerial + "\" id=\""+sessionSerial+"\" targetTable=\""+targetTable+"\">\n");
		
		
		File fil = new File(folder + "/" + fileName);
		if ( fil.exists() ) {

			logger.debug("[" + sessionSerial + "] CSV file " + fileName + " found.");
			
			xml.append("<file name=\""+fileName+"\" type=\"FILE_TYPE_CSV\" />\n");
			controlFilesList.add( folder + "/" + fileName );
			
			File filesFolder = new File( folder + "/" + "outbox" );
		    for (final File fileEntry : filesFolder.listFiles() ) {
		        if ( !fileEntry.isDirectory() ) {
		    		//xml.append("<file name=\""+fileEntry.getName()+"\" type=\"FILE_TYPE_FILE\" />\n");
		    		dataFilesList.add( folder + "/outbox/" + fileEntry.getName() );
		        }
		    }
			
		} else {
			logger.error("will not send sagi_output.txt in session.xml file: this activity instance produced no data");
		}
		

		File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath() );
		String storageRootFolder =  f.getAbsolutePath();
		storageRootFolder = storageRootFolder.substring(0, storageRootFolder.lastIndexOf( File.separator ) + 1) + "namespaces/";
		
		String folderPath = folder.replace(storageRootFolder, "").replaceAll("/+", "/");
		
		logger.debug("sending content of folder:");
		logger.debug(" > " + folderPath );
		
	    xml.append("<file name=\"session.xml\" type=\"FILE_TYPE_SESSION\" />\n");
	    
	    xml.append("<console><![CDATA[");
	    if ( task != null ) {
	    	for ( String line : task.getConsole() ) {
	    		byte pline[] = line.getBytes("UTF-8");
	    		xml.append( new String(pline, "UTF-8") + "\n" );
	    	}
	    }
	    xml.append("]]></console>");

	    
	    xml.append("<execLog><![CDATA[");
	    if ( task != null ) {
	    	for ( String line : task.getExecLog() ) {
	    		byte pline[] = line.getBytes("UTF-8");
	    		xml.append( new String(pline, "UTF-8") + "\n" );
	    	}
	    }
	    xml.append("]]></execLog>");
	    
		xml.append("</session>\n");
		controlFilesList.add( folder + "/" + "session.xml" );
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folder + "/" + "session.xml"), "UTF-8"));
		writer.write( xml.toString().replace("#TOTAL_FILES#", String.valueOf(dataFilesList.size()) ) );
		writer.close();

		logger.debug("need to send " + dataFilesList.size() + " files to HDFS...");
		uploadFiles( dataFilesList, controlFilesList, targetTable, experimentSerial, sessionSerial, folderPath );
		
		commit( macAddress );
	}
	

	private void uploadFiles( List<String> dataFilesList, List<String> controlFilesList, String targetTable, 
			String experimentSerial, String sessionSerial, String sourcePath ) throws Exception {

		// dataFilesList 	= Files in outbox folder
		// controlFilesList	= session.xml and sagi_output.txt 
		
		logger.debug("starting Multithread Uploader for session " + sessionSerial + " with " + maxUploadThreads + " threads." );
		logger.debug(" > Control files: " + controlFilesList.size() + " | Data Files: " + dataFilesList.size() );
		MultiThreadUpload mtu = new MultiThreadUpload( maxUploadThreads, hadoopConfigPath );
		mtu.upload(dataFilesList, controlFilesList, storageAddress, storagePort, 
				targetTable, experimentSerial, sessionSerial, sourcePath);
		
	}

	private void commit( String macAddress ) throws Exception {
		logger.debug("session "+sessionSerial+" commit.");
		URL url = new URL( sagiHost + "/sagitarii/transactionManager?command=commit&sessionSerial=" + sessionSerial + "&macAddress=" + macAddress );
		Scanner s = new Scanner( url.openStream() );
		String response = s.nextLine();
		logger.debug("server commit response: " + response);
		s.close();
	}
	
	private void getSessionKey( String macAddress ) throws Exception {
		URL url = new URL( sagiHost + "/sagitarii/transactionManager?command=beginTransaction&macAddress=" + macAddress);
		Scanner s = new Scanner( url.openStream() );
		sessionSerial = s.nextLine();
		logger.debug("open session " + sessionSerial );
		s.close();
	}
	
}