package br.cefetrj.sagitarii.persistence.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.cefetrj.sagitarii.persistence.entity.LogEntry;
import br.cefetrj.sagitarii.persistence.exceptions.DatabaseConnectException;
import br.cefetrj.sagitarii.persistence.exceptions.InsertException;
import br.cefetrj.sagitarii.persistence.exceptions.NotFoundException;
import br.cefetrj.sagitarii.persistence.repository.LogRepository;


public class LogService { 
	private LogRepository rep;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );
	
	public LogService() throws DatabaseConnectException {
		this.rep = new LogRepository();
	}
	
	public void newTransaction() {
		rep.newTransaction();
	}
	
	public void insertLogEntry(LogEntry logEntry) throws InsertException {
		logger.debug("inserting Log " );
		try {
			rep.insertLogEntry( logEntry );
		} catch ( Exception e ) {
			logger.error(" > " + logEntry.getLog() );
		}
	}
	
	public LogEntry getLogEntry(int idLog) throws NotFoundException{
		return rep.getLogEntry(idLog);
	}

	public void insertLogEntryList( List<LogEntry> list ) throws Exception {
		List<LogEntry> newList = new ArrayList<LogEntry>( list ); // To avoid ConcurrentModificationException
		for ( LogEntry logEntry : newList ) {
			rep.newTransaction();
			insertLogEntry(logEntry);
		}
	}
	
	public List<LogEntry> getList() throws NotFoundException {
		logger.debug( "retrieving log list..." );  
		List<LogEntry> preList = rep.getList();
		logger.debug( "done." );  
		return preList;	
	}

	public List<LogEntry> getList( String type ) throws NotFoundException {
		logger.debug( "retrieving log list..." );  
		List<LogEntry> preList = rep.getList( type );
		logger.debug( "done." );  
		return preList;	
	}
	
	public List<LogEntry> getListByActivity( String activitySerial ) throws NotFoundException {
		logger.debug( "retrieving log list..." );  
		List<LogEntry> preList = rep.getListByActivity( activitySerial );
		logger.debug( "done." );  
		return preList;	
	}
	
}