package com.Beans;

import java.util.List;

import javax.faces.bean.ManagedBean;

@ManagedBean(name = "GreenHouse")
public class GreenHouse {
	
	private String greenHouseName;
	private Plant plant;
	private List<Sensor> sensors;
	

	public String getName() {
		return greenHouseName;
	}

	public void setName(String name) {
		greenHouseName = name;
	}

	public Plant getPlant() {
		return plant;
	}

	public void setPlant(Plant plant) {
		this.plant = plant;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

}
