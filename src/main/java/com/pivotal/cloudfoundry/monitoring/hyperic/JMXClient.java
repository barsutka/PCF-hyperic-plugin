package com.pivotal.cloudfoundry.monitoring.hyperic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 * tile.
 *  
 * @since 1.4.x
 *
 */
public class JMXClient {

	/**
	 * 
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
     * Connects to a JMX endpoint using the JMX URL, Username and Password.
     * @param jmxUrl - JMX endpoint
     * @param username - user for the JMX endpoint
     * @param password - password for the JMX endpoint
     * @throws IOException
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
	 * @param queryString
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public Double getPropertyValue(String queryString) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, IOException{
		
		log.debug("Received Query String: " + queryString);
		
		String name = queryString.substring(0, queryString.lastIndexOf(":"));
		String property = queryString.substring(queryString.lastIndexOf(":")+1);
		
		log.debug("Name: " + name);
		log.debug("Property: " + property);
		
		Double value = (Double)conn.getAttribute(new ObjectName(name), property);
		return value;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<CFService> getServices(){
		
		List<CFService> cfServices = new ArrayList<CFService>();
		
    	try{
	    	log.info("Querying CF available services... using query: org.cloudfoundry:deployment=untitled_dev,job=*,index=*,*");

    		Iterator<ObjectName> names = new TreeSet<ObjectName> (conn.queryNames(new ObjectName(PCF_Query), null)).iterator();
    		while (names.hasNext()){
    			ObjectName obj = names.next();
    			
    			String serviceKind = obj.getKeyProperty("job");
    			
    			String serviceKindClassname = Character.toUpperCase(serviceKind.charAt(0)) + serviceKind.substring(1).replaceAll("-", "_");
    			
    			try{
	    			CFService cfService = (CFService) Class.forName(CFService.class.getPackage().getName()+"."+serviceKindClassname).newInstance();    			
	    			cfService.setIndex(Integer.parseInt(obj.getKeyProperty("index")));
	    			cfService.setIp(obj.getKeyProperty("ip"));
	    			//String[] partid = obj.getKeyProperty("job").split("-");
	    			//cfService.setPart(partid[2]);
	    			log.info("Found CloudFoundry service: "+serviceKind+" id: "+cfService.getIndex());    			
	    			cfServices.add(cfService);
    			}
    			catch(ClassNotFoundException e){
    				log.warn("Found CloudFoundry service: "+serviceKind+" but there's no class declared on the plugin for handling it called "+serviceKindClassname);
    				System.out.println("Found CloudFoundry service: "+serviceKind+" but there's no class declared on the plugin for handling it called "+serviceKindClassname);
    			}
    		}
    		return cfServices;
    	}
		catch(Exception e){
			log.error("ERROR while getting CF Services using JMX: "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	/*
	public List<DEA> getDeas(){
		
		List<DEA> deas = new ArrayList<DEA>();
		
    	try{
    		Iterator<ObjectName> names = new TreeSet<ObjectName> (conn.queryNames(new ObjectName("org.cloudfoundry:deployment=untitled_dev,job=DEA,index=*,ip=*"), null)).iterator();
    		while (names.hasNext()){
    			ObjectName obj = names.next();
    			DEA dea = new DEA();
    			dea.setIndex(Integer.parseInt(obj.getKeyProperty("index")));
    			dea.setIp(obj.getKeyProperty("ip"));
    			deas.add(dea);
    		}
    		return deas;
    	}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	*/
	
//	public static void main(String[] args){
//		JMXClient client = new JMXClient();
//		try {
//			client.connect("service:jmx:rmi:///jndi/rmi://192.168.5.67:44444/jmxrmi","admin","pivotal");
//			
//			echo("\nMBeanServer default domain = " + client.conn.getDefaultDomain());
//
//            // Get MBean count
//            //
//            echo("\nMBean count = " + client.conn.getMBeanCount());
//
//            // Query MBean names
//            //
//            
//            
//            echo("\nQuery MBeanServer MBeans:");
//            Set<ObjectName> names =
//                //new TreeSet<ObjectName>(client.conn.queryNames(null, null));
//            		//org.cloudfoundry:deployment=null,job=*,index=*,ip=null
//            		
//            	new TreeSet<ObjectName>(client.conn.queryNames(new ObjectName("org.cloudfoundry:deployment=untitled_dev,job=*,index=*,*"), null));	
//            for (ObjectName name : names) {
//                echo("\tObjectName = " + name);
//            }				
//            
//            List<CFService> services = client.getServices();
//            for (CFService svc : services) {
//                echo("\tService = " + svc.getClass().getSimpleName()+ " " +svc.getIndex()+" : "+svc.getIp());
//            }			        
//            
//            echo("Property value for org.cloudfoundry:deployment=untitled_dev,job=DEA,index=1,ip=10.103.44.23:available_disk_ratio[stack=lucid64]"+client.getPropertyValue("org.cloudfoundry:deployment=untitled_dev,job=DEA,index=1,ip=10.103.44.23:available_disk_ratio[stack=lucid64]"));            
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}
