package cmabreu.sagitarii.persistence.repository;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import cmabreu.sagitarii.persistence.exceptions.DatabaseConnectException;
import cmabreu.sagitarii.persistence.infra.ConnFactory;

public class BasicRepository {
	protected Session session;
	private Transaction tx = null;
	protected Logger logger;
	private String sessionId;

	public BasicRepository() throws DatabaseConnectException {
		logger = LogManager.getLogger( this.getClass().getName() );
		try {
			session = ConnFactory.getSession();
			tx = session.beginTransaction();
		} catch (Exception e ) {
			e.printStackTrace();
			logger.error( e.getMessage() );
			throw new DatabaseConnectException( e.getMessage() );
		}
        UUID uuid = UUID.randomUUID();
        sessionId = uuid.toString().toUpperCase().substring(0,8);
		logger.debug(" --- open  session " + sessionId + " ---" );
	}
	

	public void newTransaction() {
		if ( !session.isOpen() ) {
			logger.debug("new transaction for session " + sessionId );
			session = ConnFactory.getSession();
			tx = session.beginTransaction();
		} else {
			logger.debug("will not open a new transaction. session "+sessionId+" is already open");
		}
	}
	
	public boolean isOpen() {
		return session.isOpen();
	}
	
	public void closeSession() {
		logger.debug(" --- close session " + sessionId + " ---" );
		if( isOpen() ) {
			session.close();
		} else {
			logger.debug(" --- session "+sessionId+" is already closed ---" );
		}
	}
	
	public void commit() {
		logger.debug("commit session " + sessionId );
		tx.commit(); 
	}
	
	public void rollBack() {
		logger.debug("rollback session " + sessionId);
		tx.rollback();
	}
	

}