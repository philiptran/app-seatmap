package cl.seatmap.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author philiptrannp
 * 
 */
public class ExchangeContact {
	private String name;
	private String title;
	private String email;
	private String department;
	private String phone;
	private String mobile;
	private String officeLocation;
	private String country;
	private ContactLocation contactLocation;
	private List<ContactLocation> nearby = new ArrayList<ContactLocation>();

	public ExchangeContact(String name, String title, String email,
			String department, String phone, String mobile, String location,
			String country) {
		this.name = nullSafe(name);
		this.title = nullSafe(title);
		this.email = nullSafe(email);
		this.department = nullSafe(department);
		this.phone = nullSafe(phone);
		this.mobile = nullSafe(mobile);
		this.officeLocation = nullSafe(location);
		this.country = nullSafe(country);
	}

	private String nullSafe(String s) {
		return s == null || "null".equals(s) ? "" : s;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title == null ? "" : title;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getOfficeLocation() {
		return officeLocation;
	}

	public void setOfficeLocation(String officeLocation) {
		this.officeLocation = officeLocation;
	}

	public ContactLocation getContactLocation() {
		return contactLocation;
	}

	public void setContactLocation(ContactLocation contactLocation) {
		this.contactLocation = contactLocation;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<ContactLocation> getNearby() {
		return nearby;
	}

	public void setNearby(List<ContactLocation> nearby) {
		this.nearby = nearby;
	}

}
