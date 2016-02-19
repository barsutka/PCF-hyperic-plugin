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

import com.pivotal.cloudfoundry.monitoring.hyperic.services.CFService;

/**
 * This class is called during auto-discovery in Hyperic and is an extension
 * of the Hyperic framework. The PCF plugin is responsible for creating its 
 * own server and service resources to be displayed in the Hyperic UI.
 * 
 * @version 1.5.x
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
        
       	
    	log.info("[getServerResources] PCF:PLUGIN platfromConfig=" + platformConfig);
    	    	
        
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
    	
    	log.debug("PCF:PLUGIN JMX URL=" + jmxURL);
    	log.debug("PCF:PLUGIN username=" + username);
    	log.debug("PCF:PLUGIN password=" + password);

    	JMXClient client = JMXClient.getInstance();
    	try {
			client.connect(jmxURL, username, password);
			serverConfig.setValue("Availability", true);
	    	log.info("[discoverServices] PCF:PLUGIN Connected to JMX");

		} catch (Exception e) {
			e.printStackTrace();
			log.info("[discoverServices] PCF:PLUGIN "+e.getMessage());
			log.info("[discoverServices] PCF:PLUGIN ERROR CONNECTING TO JMX. WILL SET AVAILABILITY TO FALSE");

			serverConfig.setValue("Availability", false);
			return new ArrayList();
		}	
   
    	List<ServiceResource> services = new ArrayList<ServiceResource>();
  
    	// Convert the MBeans list to Hyperic Service Resources.
    	Iterator<CFService> cfServices = client.getServices().iterator();
    	while (cfServices.hasNext()){    		
    		CFService cfService = cfServices.next();
          
    		ServiceResource service = createServiceResource(cfService.getType());
            service.setName(cfService.getType() + " index:" +  cfService.getIndex() + " ip:" + cfService.getIp());
            ConfigResponse productConfig = new ConfigResponse();
            productConfig.setValue("service.ID", cfService.getIndex());
            productConfig.setValue("service.IP", cfService.getIp());
            productConfig.setValue("service.Job", cfService.getJob());
            setProductConfig(service, productConfig);
            setMeasurementConfig(service, new ConfigResponse());
            services.add(service);    		
            log.info("PCF:PLUGIN Added service: "+service.getType()+" service.ID "+cfService.getIndex() + " name=" + service.getName() + " service.Job=" + cfService.getJob());
    	}
    	
    	log.info("PCF:PLUGIN Returning services #: "+services.size());

    	return services;
    	
    }
}
