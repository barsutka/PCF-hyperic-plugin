package com.pivotal.cloudfoundry.monitoring.hyperic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;

import com.pivotal.cloudfoundry.monitoring.hyperic.services.CF1Service;

/**
 * This class is called during auto-discovery in Hyperic and is an extension
 * of the Hyperic framework. The PCF plugin is responsible for creating its 
 * own server and service resources to be displayed in the Hyperic UI.
 * 
 * @version 1.4.x
 */

public class Discovery extends ServerDetector implements AutoServerDetector {

    /**
     * Logging for this class
     */
	private static Log log = LogFactory.getLog(Discovery.class);

	/**
	 * Creates the list of Server Resources for Pivotal Cloud Foundry plugin.
	 * Since all PCF components are treated as services and not servers,
	 * this method only creates 1 server.
	 */
    public List getServerResources(ConfigResponse platformConfig) throws PluginException {
        
       	
    	log.info("[getServerResources] platfromConfig=" + platformConfig);
    	    	
        
        List servers = new ArrayList();
        for (int i = 1; i <= 2; i++) {
            ServerResource server = createServerResource("Remote PCF");
            server.setName(server.getName());
            ConfigResponse productConfig = new ConfigResponse();
           // productConfig.setValue("process.ID", i);
            setProductConfig(server, productConfig);
            setMeasurementConfig(server, new ConfigResponse());
            servers.add(server);
        }
        return servers;
    }

    /**
     * Called during the auto-discovery phase, this method gathers all the available
     * MBeans using the {@link JMXClient#getServices()} method. The retruned list is then
     * converted into Hyperic Service Resources.
     */
    @Override
    protected List discoverServices(ConfigResponse serverConfig) throws PluginException {
        
    	log.info("[discoverServices] serverConfig=" + serverConfig);
        
    	String jmxURL = serverConfig.getValue("JMX_URL");
    	String username = serverConfig.getValue("Username");
    	String password = serverConfig.getValue("Password");
    	
    	log.debug("JMX URL=" + jmxURL);
    	log.debug("username=" + username);
    	log.debug("password=" + password);

    	JMXClient client = JMXClient.getInstance();
    	try {
			client.connect(jmxURL, username, password);
			serverConfig.setValue("Availability", true);
	    	log.info("[discoverServices] Connected to JMX");

		} catch (Exception e) {
			e.printStackTrace();
			log.info("[discoverServices] "+e.getMessage());
			log.info("[discoverServices] EXCEPTION CONNECTING TO JMX. WILL SET AVAILABILITY TO FALSE");

			serverConfig.setValue("Availability", false);
			return new ArrayList();
		}	
   
    	List<ServiceResource> services = new ArrayList<ServiceResource>();
  
    	// Convert the MBeans list to Hyperic Service Resources.
    	Iterator<CF1Service> cfServices = client.getServices().iterator();
    	while (cfServices.hasNext()){    		
    		CF1Service cfService = cfServices.next();
          
    		ServiceResource service = createServiceResource(cfService.getJob());
            service.setName(cfService.getJob() + " index:" +  cfService.getIndex() + " ip:" + cfService.getIp());
            ConfigResponse productConfig = new ConfigResponse();
            productConfig.setValue("service.ID", cfService.getIndex());
            productConfig.setValue("service.IP", cfService.getIp());
            setProductConfig(service, productConfig);
            setMeasurementConfig(service, new ConfigResponse());
            services.add(service);    		
            log.info("Added service: "+service.getType()+" service.ID "+cfService.getIndex() + " name= " + service.getName());
    	}
    	
    	log.info("Returning services #: "+services.size());

    	return services;
    	
    }
}
