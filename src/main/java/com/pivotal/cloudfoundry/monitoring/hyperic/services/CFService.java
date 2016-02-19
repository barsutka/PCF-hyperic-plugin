package com.pivotal.cloudfoundry.monitoring.hyperic.services;

public class CFService {
	
	private String serviceTypePrefix = "Pivotal CloudFoundry 1.6.x";
	private String Job;
	private int index;
	private String ip;
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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
