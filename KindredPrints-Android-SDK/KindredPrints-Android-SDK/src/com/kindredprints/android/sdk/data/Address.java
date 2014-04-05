package com.kindredprints.android.sdk.data;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class Address {
	public static final String ADDRESS_VALUE_NONE = "no_address_value";
	
	private String addressId;
	private String shipMethod;
	private String name;
	private String street;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String phone;
	private String email;
	
	public Address () {
		this.addressId = ADDRESS_VALUE_NONE;
		this.shipMethod = ADDRESS_VALUE_NONE;
		this.name = ADDRESS_VALUE_NONE;
		this.street = ADDRESS_VALUE_NONE;
		this.city = ADDRESS_VALUE_NONE;
		this.state = ADDRESS_VALUE_NONE;
		this.zip = ADDRESS_VALUE_NONE;
		this.country = ADDRESS_VALUE_NONE;
		this.phone = ADDRESS_VALUE_NONE;
		this.email = ADDRESS_VALUE_NONE;
	}
	
	public Address(String gsonPacked) {
		Type addressType = new TypeToken<Address>() {}.getType();
		Address addr = new Gson().fromJson(gsonPacked, addressType);
		
		this.addressId = addr.getAddressId();
		this.shipMethod = addr.getShipMethod();
		this.name = addr.getName();
		this.street = addr.getStreet();
		this.city = addr.getCity();
		this.state = addr.getState();
		this.zip = addr.getZip();
		this.country = addr.getCountry();
		this.phone = addr.getPhone();
		this.email = addr.getEmail();
	}
	
	public Address copy() {
		Address newCopy = new Address();
		newCopy.setAddressId(this.getAddressId());
		newCopy.setShipMethod(this.getShipMethod());
		newCopy.setName(this.getName());
		newCopy.setStreet(this.getStreet());
		newCopy.setCity(this.getCity());
		newCopy.setState(this.getState());
		newCopy.setZip(this.getZip());
		newCopy.setCountry(this.getCountry());
		newCopy.setPhone(this.getPhone());
		newCopy.setEmail(this.getEmail());
		return newCopy;
	}
	
	public String packAddress() {
		Type addressType = new TypeToken<Address>() {}.getType();
		String serializedAddress = new Gson().toJson(this, addressType);
		return serializedAddress;
	}
	
	public String getShipMethod() {
		return shipMethod;
	}

	public void setShipMethod(String shipMethod) {
		this.shipMethod = shipMethod;
	}
	
	public String getAddressId() {
		return addressId;
	}

	public void setAddressId(String id) {
		this.addressId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
