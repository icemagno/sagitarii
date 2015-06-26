package cmabreu.sagitarii.core.processor;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cmabreu.sagitarii.core.Cluster;
import cmabreu.sagitarii.core.ClustersManager;
import cmabreu.sagitarii.core.Sagitarii;
import cmabreu.sagitarii.core.config.Configurator;
import cmabreu.sagitarii.persistence.entity.Activity;
import cmabreu.sagitarii.persistence.entity.Instance;
import cmabreu.sagitarii.persistence.exceptions.NotFoundException;
import cmabreu.sagitarii.persistence.services.ActivityService;

/**
 * O Pseudo Cluster executará tarefas locais, como acesso a dados.
 * Atividades do tipo QUERY.
 * 
 * @author Carlos Magno Abreu
 *
 */
public class MainCluster implements Runnable {
	private	Logger logger = LogManager.getLogger( this.getClass().getName() );
	private int maxAllowedTasks = 4;
	private int currentTaskCount = 0;
	private XMLParser parser;
	private String macAddress;

	public MainCluster( int maxAllowedTasks, String macAddress ) {
		this.maxAllowedTasks = maxAllowedTasks;
		this.parser = new XMLParser();
		this.macAddress = macAddress;
	}
	
	
    private double getProcessCpuLoad() {
    	double result = 0;
    	try {
    		result = Configurator.getInstance().getProcessCpuLoad();
    	} catch ( Exception e ) {
    		
    	}
    	return result;
    }   	
	
	@Override
	public void run() {
		long freeMemory = 0;
		long totalMemory = 0;
		try {
			freeMemory = Configurator.getInstance().getFreeMemory();
			totalMemory = Configurator.getInstance().getTotalMemory();
		} catch ( Exception e ) {  }
			
		Cluster cluster = ClustersManager.getInstance().addOrUpdateCluster("0.0", "Local System", macAddress, "Local Machine", "Sagitarii Server", getProcessCpuLoad(), "Main Cluster", 8, maxAllowedTasks, freeMemory, totalMemory );
		try {
			if ( cluster != null ) {
				cluster.setAsMainCluster();
				if ( currentTaskCount < maxAllowedTasks ) {
					Instance pipe = Sagitarii.getInstance().getNextJoinInstance();
					if ( pipe != null ) {

						String content = pipe.getContent().replace("##TAG_ID_INSTANCE##", String.valueOf( pipe.getIdInstance() ) );
						content = content.replace("%ID_PIP%", String.valueOf( pipe.getIdInstance() ) ); 
						pipe.setContent( content );
						
						currentTaskCount++;
						logger.debug("will process instance " + pipe.getSerial() );
						cluster.addInstance(pipe);
						List<Activation> acts = parser.parseActivations( pipe.getContent() );
						if ( acts.size() > 0 ) {
							Activation act = acts.get(0);
							ClustersManager.getInstance().acceptTask( pipe.getSerial(), macAddress);
							
							MainClusterQueryWrapper mcqw = new MainClusterQueryWrapper();
							mcqw.executeQuery( act );
							
							ActivityService as = new ActivityService();
							try {
								Activity activity = as.getActivity( act.getActivitySerial() );
								cluster.setInstanceAsDone( pipe.getSerial(), activity );
							} catch ( NotFoundException nf ) {	
								logger.error("cannot finish instance " + pipe.getSerial() + ". Activity " + act.getActivitySerial() + " not found." );
							}
							
						}

					}
				}
			}

		} catch ( Exception e ) {
			cluster.setMessage( e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	

}