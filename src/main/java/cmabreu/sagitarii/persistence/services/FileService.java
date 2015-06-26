package cmabreu.sagitarii.persistence.services;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cmabreu.sagitarii.persistence.entity.File;
import cmabreu.sagitarii.persistence.entity.FileLight;
import cmabreu.sagitarii.persistence.exceptions.DatabaseConnectException;
import cmabreu.sagitarii.persistence.exceptions.DeleteException;
import cmabreu.sagitarii.persistence.exceptions.InsertException;
import cmabreu.sagitarii.persistence.exceptions.NotFoundException;
import cmabreu.sagitarii.persistence.repository.FileRepository;

public class FileService {
	private FileRepository rep;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	public FileService() throws DatabaseConnectException {
		this.rep = new FileRepository();
	}

	public FileLight getFileLight(int idFile) throws NotFoundException{
		return rep.getFileLight(idFile);
	}

	public File getFile(int idFile) throws NotFoundException{
		return rep.getFile(idFile); 
	}

	public void newTransaction() {
		if( !rep.isOpen() ) {
			rep.newTransaction();
		}
	}
	
	public File insertFile(File file) throws InsertException {
		File expRet = rep.insertFile( file );
		return expRet ;
	}	
	
	public void deleteFile( int idFile ) throws DeleteException {
		try {
			FileLight file = rep.getFileLight(idFile);
			rep.newTransaction();
			rep.deleteFile(file);
		} catch (NotFoundException e) {
			logger.error( e.getMessage() );
			throw new DeleteException( e.getMessage() );
		}
	}

	public Set<FileLight> getList( int idExperiment ) throws NotFoundException {
		Set<FileLight> preList = rep.getList( idExperiment );
		return preList;	
	}

	public Set<FileLight> getList( int idExperiment, String activityTag, String rangeStart, String rangeEnd ) throws NotFoundException {
		Set<FileLight> preList = rep.getList( idExperiment, activityTag, rangeStart, rangeEnd );
		return preList;	
	}

	
}