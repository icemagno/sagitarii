package br.cefetrj.sagitarii.core.filetransfer;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.cefetrj.sagitarii.metrics.MetricController;
import br.cefetrj.sagitarii.metrics.MetricType;
import br.cefetrj.sagitarii.misc.PathFinder;

public class Server {
	private ServerSocket serverSocket;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );
	private List<FileImporter> importers;
	private FtpServer server;
	
	public List<FileImporter> getImporters() {
		return new ArrayList<FileImporter>( importers );
	}
	
	public String beginTransaction( String macAddress ) throws Exception {
		String sessionSerial = UUID.randomUUID().toString().replace("-", "");
		String cacheDirectory = PathFinder.getInstance().getPath() + "/cache/" + sessionSerial + "/";
		new File(cacheDirectory).mkdirs();
		logger.debug("starting session " + sessionSerial + " to node " + macAddress );
		MetricController.getInstance().hit( "Session Open", MetricType.FILE );
		return sessionSerial;
	}

	
	public void closeTransaction( String sessionSerial ) throws Exception {
		logger.debug("will delete session folder " + sessionSerial + "....");
		String directory = PathFinder.getInstance().getPath() + "/cache/" + sessionSerial;
		FileUtils.deleteDirectory( new File( directory ) );
		MetricController.getInstance().hit( "Session Close", MetricType.FILE );
		logger.debug("done deleting session folder " + sessionSerial );
	}	

	private boolean checkSession( String sessionSerial ) throws Exception {
		Path cacheDirectory = new File(PathFinder.getInstance().getPath() + "/cache/" + sessionSerial + "/").toPath();
		return Files.exists( cacheDirectory, LinkOption.NOFOLLOW_LINKS);
	}
	
	public String commit( String sessionSerial ) throws Exception {
		logger.debug("will commit session " + sessionSerial );
		
		if ( !checkSession(sessionSerial) ) {
			throw new Exception("Session not opened.");
		}
		
		FileImporter fi = new FileImporter( sessionSerial, this );
		fi.setName("Sagitarii session importer: " + sessionSerial );
		fi.start();
		importers.add(fi);
		return "ok";
	}
	
	public Server( int serverPort, int chunkBuffer ) throws Exception {
		logger.debug("start");

		this.importers = new ArrayList<FileImporter>();
		
		try {
	        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
	        UserManager userManager = userManagerFactory.createUserManager();

	        List<Authority> authorities = new ArrayList<Authority>();
	        authorities.add( new WritePermission() );
	        authorities.add( new ConcurrentLoginPermission(5000,5000) );
	        
	        BaseUser user = new BaseUser();
	        user.setName("cache");
	        user.setPassword("cache");
	        user.setHomeDirectory( PathFinder.getInstance().getPath() + "/cache/" );
	        user.setAuthorities(authorities);
	        user.setMaxIdleTime(1000);
	        user.setEnabled( true );
	        userManager.save( user );
	         
	        BaseUser scientist = new BaseUser();
	        scientist.setName("storage");
	        scientist.setPassword("storage");
	        scientist.setHomeDirectory( PathFinder.getInstance().getPath() + "/storage/" );
	        scientist.setAuthorities(authorities);
	        scientist.setMaxIdleTime(1000);
	        scientist.setEnabled(true);
	        userManager.save( scientist );
	
	        InetAddress address = getFirstNonLoopbackAddress(true, false);
	        String externalIpAddress = address.getHostAddress();
	        
	        logger.debug("Bind FTP address to " + externalIpAddress);
	        
	        ListenerFactory listenerFactory = new ListenerFactory();
	        DataConnectionConfigurationFactory dataConnectionFactory = new DataConnectionConfigurationFactory();
	        dataConnectionFactory.setPassivePorts( "0-" );
	        dataConnectionFactory.setActiveLocalAddress(externalIpAddress);
	        dataConnectionFactory.setPassiveAddress(externalIpAddress);
	        dataConnectionFactory.setPassiveExternalAddress(externalIpAddress);
	        
	        listenerFactory.setDataConnectionConfiguration( dataConnectionFactory.createDataConnectionConfiguration() );
	        listenerFactory.setPort( serverPort );
	        Listener listener = listenerFactory.createListener();
	        
	        Map<String, Ftplet> m = new HashMap<String, Ftplet>();
	        m.put("miaFtplet", new FtpletMonitor() );

	        FtpServerFactory factory = new FtpServerFactory(); 
	        factory.setUserManager(userManager);
	        factory.addListener("default", listener );
	        factory.setFtplets(m);
	        
	        server = factory.createServer();
	        server.start();
	        
		} catch ( Exception e ) {
			logger.error("error when starting server: " + e.getMessage() );
			e.printStackTrace();
		}
		
	}

    private InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }    

	public void clean() {
		List<FileImporter> importersToRemove = new ArrayList<FileImporter>();
		
		logger.debug("clean up");
		
		logger.debug("checking importers to remove...");
		for ( FileImporter importer : getImporters() ) {
			try {
				if ( importer.getStatus().equals("DONE") ) {
					logger.debug(" > will remove " + importer.getName() );
					importersToRemove.add( importer );
				}
			} catch ( Exception e ) {
				e.printStackTrace();
				importersToRemove.add( importer );
			}
			
			
		}
		logger.debug("will clean " + importersToRemove.size() + " importers");
		try {
			importers.removeAll( importersToRemove );	
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
		logger.debug("done");
		
	}
	
	public void stopServer() {
		logger.debug("stop");
		server.stop();
		try {
			serverSocket.close();
		} catch ( Exception ignored ) { }
	}


}