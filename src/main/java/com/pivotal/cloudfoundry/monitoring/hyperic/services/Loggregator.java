package com.pivotal.cloudfoundry.monitoring.hyperic.services;


public class Loggregator implements CFService{

	private int index;
	private String ip;
	private String partition;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String job) {
		this.ip = job;
	}
	
	public String getPart() {
		return partition;
	}
	
	public void setPart(String partition) {
		this.partition = partition;
	}
	
	
}
