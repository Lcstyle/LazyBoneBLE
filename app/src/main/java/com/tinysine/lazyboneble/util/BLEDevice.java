package com.tinysine.lazyboneble.util;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class BLEDevice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private String address;

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BLEDevice(String name, String address) {
		super();
		this.name = name;
		this.address = address;
	}

	public BLEDevice() {
		super();
	}

	@NonNull
	@Override
	public String toString() {
		return "BLEDevice [name=" + name + ", address=" + address + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BLEDevice other = (BLEDevice) obj;
		if (address == null) {
			return other.address == null;
		} else return address.equals(other.address);
	}

}
