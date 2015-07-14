package com.pivotal.cloudfoundry.monitoring.hyperic;

public class MBeanTuple {
	
	private String Name;
	private String Property;
	
	public MBeanTuple(String name, String property)
	{
		this.Name = name;
		this.Property = property;
	}

	public String getName() {
		return Name;
	}

	public String getProperty() {
		return Property;
	}
	

}
