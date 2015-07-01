package com.pivotal.cloudfoundry.monitoring.hyperic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class JMXClientTest {
	
	private String jmxURL = "service:jmx:rmi:///jndi/rmi://192.168.5.67:44444/jmxrmi";
	private String user = "admin";
	private String password = "pivotal";
	private JMXClient client = null;

	@Before
	public void JMXClient()
	{
		client = JMXClient.getInstance();
	}
	
	@Test
	public void testGetInstance() {
		JMXClient client = JMXClient.getInstance();
		assertNotNull(getClass());
	}

	@Test
	public void testConnect() {
		try
		{
			client.connect(this.jmxURL, this.user, this.password);
		}
		catch (Exception ex)
		{
			fail("Exception throw connecting to JMX endpoint. Validate endpoint is correct");
		}
	}

	@Test
	public void testIsConnected() {
		try
		{
			client.connect(this.jmxURL, this.user, this.password);
		}
		catch (Exception ex)
		{
			fail("Exception throw connecting to JMX endpoint. Validate endpoint is correct");
		}
		assertTrue(client.isConnected());
	}

	@Test
	public void testGetPropertyValue() {
		// client.getPropertyValue("test:test")
		//assertTrue(services.size() > 0);
	}

	@Test
	public void testGetServices() {
		try{
			client.connect(this.jmxURL, this.user, this.password);
		}
		catch (Exception e)
		{
			fail("unable to connect to JMX endpoint");
		}
		List services = client.getServices();
		assertTrue(services.size() > 0);
	}

}
