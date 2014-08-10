package com.learn.support;

import java.util.ArrayList;

public class VenuePojo {

	String name, photo,id;
	Double longitude, latitude;
	ArrayList<String> address= new ArrayList<String>();

	public VenuePojo(String id,Double longitude, Double latitude, String name,
			String photo, ArrayList<String> address,String photoPrefix,String photoSufix) {
		this.id=id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.name = name;
		this.photo = photoPrefix+"75x75"+photoSufix;
		this.address = address;
	}

	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public Double getLongitude() {
		return longitude;
	}



	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}



	public Double getLatitude() {
		return latitude;
	}



	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public ArrayList<String> getAddress() {
		return address;
	}

	public void setAddress(ArrayList<String> address) {
		this.address = address;
	}

}
