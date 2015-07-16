package com.pivotal.cloudfoundry.monitoring.hyperic.services;

public class CFService {
	
	private String Job;
	private int index;
	private String ip;
	
	public void setJob(String job) {
		Job = job;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getJob() {
		return Job;
	}
	public int getIndex() {
		return index;
	}
	public String getIp() {
		return ip;
	}

}
