package br.cefetrj.sagitarii.teapot.comm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MultiThreadUpload {
	private int maxThreadsRunning; 
	private long totalBytes = 0;
	private String hadoopConfigPath;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );
	
	public MultiThreadUpload( int maxThreadsRunning, String hadoopConfigPath ) {
		this.maxThreadsRunning = maxThreadsRunning;
		this.hadoopConfigPath = hadoopConfigPath;
	}
	
	public long getTotalBytes() {
		return totalBytes;
	}
	
	public void upload( List<String> dataFilesList, List<String> controlFilesList, String storageAddress, 
			int storagePort, String targetTable, String experimentSerial, 
			String sessionSerial, String sourcePath ) throws Exception {
		
		// dataFilesList 	= Files in outbox folder
		// controlFilesList	= session.xml and sagi_output.txt		
		
		if ( maxThreadsRunning == 0 ) { maxThreadsRunning = 7; }
		
		List< FutureTask<Long> > futureTasks = new ArrayList< FutureTask<Long> >();
		ExecutorService executor = Executors.newFixedThreadPool( maxThreadsRunning );

		// Send CSV data and XML manifest to sagitarii via FTP ( controlFilesList )
		logger.debug(" > starting FTP upload thread with " + controlFilesList.size() + " elements for session " + sessionSerial + 
				" / " + sourcePath);
		FTPUploadTask fut = new FTPUploadTask(controlFilesList, storageAddress, storagePort, 
				targetTable, experimentSerial, sessionSerial, sourcePath);
		FutureTask<Long> futureTask = new FutureTask<Long>( fut );
		executor.execute( futureTask );
		futureTasks.add( futureTask );
		
		
		// Send Task files to HDFS ( dataFilesList )
		logger.debug(" > starting " + maxThreadsRunning + " HDFS upload threads with " + dataFilesList.size() + " elements for session " + sessionSerial + 
				" / " + sourcePath);
		if ( dataFilesList.size() > 0 ) {
			List<List<String>> partitions = splitList( dataFilesList );
			int totalFiles = dataFilesList.size();
			
			if( partitions.size() < maxThreadsRunning ) {
				maxThreadsRunning = partitions.size(); 
			}
			
			logger.debug("Multithread Uploader started to send " + totalFiles + " files splited in " + partitions.size() +
					" partitions limited to " + maxThreadsRunning + " threads.");
			
			for( List<String> list : partitions ) {
				logger.debug(" > starting upload thread with " + list.size() + " elements for session " + sessionSerial + 
						" / " + sourcePath);
				
				HDFSUploadTask futHdfs = new HDFSUploadTask(list, hadoopConfigPath, 
						targetTable, experimentSerial, sessionSerial, sourcePath);
				FutureTask<Long> futureTaskHdfs = new FutureTask<Long>( futHdfs );
				executor.execute( futureTaskHdfs );
				futureTasks.add( futureTaskHdfs );
			}
		}
		
		
		logger.debug("waiting to all threads to finish...");
		while ( true ) {
            try {
            	boolean done = true;
            	for ( FutureTask<Long> ft : futureTasks ) {
            		done = ( done && ( ft.isDone() || ft.isCancelled() ) ); 
            		if ( ft.isDone() ) {
            			//totalBytes = totalBytes + ft.get();
            		}
            	}
            	if ( done ) break;
            } catch ( Exception e ) {
            	logger.error("thread error: " + e.getMessage() );
            }
		}
		executor.shutdown();
		logger.debug("all threads finished. Multithread Uploader shutdown. ");
	}
	
	public List<List<String>> splitList( List<String> list ) {
		List<List<String>> partitions = new LinkedList<List<String>>();
		
		if ( list.size() <= maxThreadsRunning ) {
			partitions.add( list );
		} else {
			int partitionSize = list.size() / maxThreadsRunning;
			for (int i = 0; i < list.size(); i += partitionSize) {
				partitions.add(list.subList(i,
						Math.min(i + partitionSize, list.size())));
			}
		}
		
		return partitions;
	}
	
}
