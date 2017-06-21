package com.Beans;

import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

@ManagedBean(name = "Environment")
public class Environment {
	@ManagedProperty(value = "#{param.greenHouseName}")
	private String greenHouseName;
	private String plantName;
	private String temperatureSensors;
	private String moistureSensors;
	private Date seedingMoment;
	private Date startGermination;
	private Date endGermination;
	private Date startVegetation;
	private Date endVegetation;
	private String expectations;
	
	
	public String getExpectations() {
		return expectations;
	}
	public void setExpectations(String expectations) {
		this.expectations = expectations;
	}
	public String getGreenHouseName() {
		return greenHouseName;
	}
	public void setGreenHouseName(String greenHouseName) {
		this.greenHouseName = greenHouseName;
	}
	public String getPlantName() {
		return plantName;
	}
	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}
	public String getTemperatureSensors() {
		return temperatureSensors;
	}
	public void setTemperatureSensors(String temperatureSensors) {
		this.temperatureSensors = temperatureSensors;
	}
	public String getMoistureSensors() {
		return moistureSensors;
	}
	public void setMoistureSensors(String moistureSensors) {
		this.moistureSensors = moistureSensors;
	}
	public Date getSeedingMoment() {
		return seedingMoment;
	}
	public void setSeedingMoment(Date seedingMoment) {
		this.seedingMoment = seedingMoment;
	}
	public Date getEndGermination() {
		return endGermination;
	}
	public void setEndGermination(Date endGermination) {
		this.endGermination = endGermination;
	}
	public Date getStartGermination() {
		return startGermination;
	}
	public void setStartGermination(Date startGermination) {
		this.startGermination = startGermination;
	}
	public Date getStartVegetation() {
		return startVegetation;
	}
	public void setStartVegetation(Date startVegetation) {
		this.startVegetation = startVegetation;
	}
	public Date getEndVegetation() {
		return endVegetation;
	}
	public void setEndVegetation(Date endVegetation) {
		this.endVegetation = endVegetation;
	}
}
