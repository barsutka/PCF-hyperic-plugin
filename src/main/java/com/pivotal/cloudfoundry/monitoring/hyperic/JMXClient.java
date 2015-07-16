package com.pivotal.cloudfoundry.monitoring.hyperic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pivotal.cloudfoundry.monitoring.hyperic.services.CFService;

/**
 * This class represents a JMXClient connection to the Pivotal Ops Metric
 * tile. This class is used by both the Discovery class and the Measurement
 * class to interact with the JMX endpoint and retrieve information.
 *  
 * @since 1.4.x
 *
 */
public class JMXClient {

	/**
	 * The JMX endpoint connection.
	 */
	MBeanServerConnection conn;
	
	/**
	 * Used for Logging
	 */
    private static Log log = LogFactory.getLog(JMXClient.class);
    /**
     * Singleton instance of the JMXClient
     */
	private static JMXClient instance = null;
	
	/**
	 * Query String defined by Pivotal Ops Metrics 
	 */
	private static String PCF_Query = "org.cloudfoundry:deployment=untitled_dev,job=*,index=*,*";
	
	/**
	 * Constructor for JMXClient
	 */
    private JMXClient(){
    	
    }
    
    /**
     * Returns the JMXClient singleton reference.
     * @return JMXClient
     */
    public static JMXClient getInstance(){
    	if (instance==null) instance = new JMXClient();
    	return instance;
    }
    
    /**
     * Connects to a JMX endpoint using the JMX URL, User name and Password. 
     * These values are part of the Hyperic plugin configuration defined in the 
     * hq-plugin.xml file.
     *  
     * @param jmxUrl - JMX endpoint
     * @param username - user for the JMX endpoint
     * @param password - password for the JMX endpoint
     * @throws IOException - If the connection fails
     */
	public void connect(String jmxUrl, String username, String password) throws IOException{
			
		log.debug("JMX_URL: " + jmxUrl);
		log.debug("Username: " + username);
		log.debug("Password: " + password);
		
		if (jmxUrl==null || username==null || password==null) return;

		Map<String,Object> properties = new HashMap<String, Object>();
		String[] credentials = {username,password}; 
		properties.put(JMXConnector.CREDENTIALS, credentials);


		JMXServiceURL url =
				new JMXServiceURL(jmxUrl);
		JMXConnector jmxc = JMXConnectorFactory.connect(url, properties);

		conn = jmxc.getMBeanServerConnection();

	}
	
	/**
	 * Verify if the JMX Client is connected.
	 * @return True if connected
	 */
	public boolean isConnected(){
		return (conn!=null);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	MBeanTuple decomposeQueryString(String query)
	{
		MBeanTuple tuple = null;
		log.debug("Received Query String: " + query);
		
		String name = query.substring(0, query.lastIndexOf(":"));
		String property = query.substring(query.lastIndexOf(":")+1);
		
		log.debug("Name: " + name);
		log.debug("Property: " + property);
		
		tuple = new MBeanTuple(name, property);
		return tuple;
	}
	
	/**
	 * This method decomposes the template string defined in hq-plugin.xml for each
	 * service defined and creates a query string.
	 * 
	 * @param queryString - Defined in the hq-plugin.xml file as the template
	 * @return value fom JMX endpoint for metric.
	 * 
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public Double getPropertyValue(String queryString) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, IOException{
		MBeanTuple tuple = decomposeQueryString(queryString);
		Double value = (Double)conn.getAttribute(new ObjectName(tuple.getName()), tuple.getProperty());
		return value;
	}
	
	/**
	 * 
	 * @return
	 */
	Set<ObjectName> getMBeans()
	{
		log.info("Querying CF available services query: " + PCF_Query);
		Set<ObjectName> mBeans = null;
		try {
			mBeans = (conn.queryNames(new ObjectName(PCF_Query), null));
		} catch (MalformedObjectNameException | IOException e) {
			// TODO Validate result
			e.printStackTrace();
		}
		
		return mBeans;
	}
	
	/**
	 * 
	 * @param mBeans
	 * @return
	 */
	List<CFService> createCFServiceList(Set<ObjectName> mBeans)
	{
		List<CFService> services = new ArrayList<CFService>();
		Iterator<ObjectName> it = mBeans.iterator();
		while(it.hasNext())
		{
			ObjectName obj = it.next();
			CFService cfService = new CFService();
			cfService.setJob(obj.getKeyProperty("job"));
			cfService.setIndex(Integer.parseInt(obj.getKeyProperty("index")));
			cfService.setIp(obj.getKeyProperty("ip"));
			services.add(cfService);
			log.info("Adding Cloud Foundry Service: " + obj.getCanonicalName());
		}
		return services;
	}
	
	/**
	 * Called during the auto-discovery phase, this method retrieves a list of MBeans
	 * from Pivotal Cloud Foundry Ops Metrics and creates an internal set of CFService
	 * objects to represent these MBeans.
	 * 
	 * @return - List of Cloud Foundry Services
	 */
	public List<CFService> getServices(){
		
		List<CFService> cfServices = new ArrayList<CFService>();
		cfServices = createCFServiceList(getMBeans());
   		return cfServices;
	}
}
