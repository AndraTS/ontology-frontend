package com.Beans;

public class Plant {
	
	private String plantName = "";
	private OptimalConditions optimalConditions;

	public OptimalConditions getOptimalConditions() {
		return optimalConditions;
	}

	public void setOptimalConditions(OptimalConditions optimalConditions) {
		this.optimalConditions = optimalConditions;
	}

	public String getName() {
		return plantName;
	}

	public void setName(String name) {
		plantName = name;
	}

}
