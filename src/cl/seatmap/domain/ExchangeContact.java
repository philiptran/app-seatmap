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
    private String email;
    private String department;
    private String phone;
    private String mobile;
    private String officeLocation;
    private ContactLocation contactLocation;
    private List<ContactLocation> nearby = new ArrayList<ContactLocation>();

    public ExchangeContact(String name, String email, String department, String phone, String mobile, String officeLocation) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.phone = phone;
        this.mobile = mobile;
        this.officeLocation = officeLocation;
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

	public List<ContactLocation> getNearby() {
		return nearby;
	}

	public void setNearby(List<ContactLocation> nearby) {
		this.nearby = nearby;
	}
    
}
