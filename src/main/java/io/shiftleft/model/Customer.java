package io.shiftleft.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Customer {
  public Customer() {
  }

public Customer(String customerId, int clientId, String firstName, String lastName, Date dateOfBirth, String ssn,
      String socialInsurancenum, String tin, String phoneNumber, Address address, Set<Account> accounts) {
    super();
    
    // Validate and sanitize inputs during object construction
    if (customerId != null && customerId.length() > 255) {
        throw new IllegalArgumentException("Customer ID exceeds maximum length");
    }
    if (firstName != null && firstName.length() > 255) {
        throw new IllegalArgumentException("First name exceeds maximum length");
    }
    if (lastName != null && lastName.length() > 255) {
        throw new IllegalArgumentException("Last name exceeds maximum length");
    }
    
    this.clientId = clientId;
    this.customerId = customerId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.ssn = ssn;
    this.socialInsurancenum = socialInsurancenum;
    this.tin = tin;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.accounts = accounts;
}


  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String customerId;

  private int clientId;

  private String firstName;

  private String lastName;

  private Date dateOfBirth;

  private String ssn;

  private String socialInsurancenum;

  private String tin;

  private String phoneNumber;

  @OneToOne(cascade = { CascadeType.ALL })
  private Address address;

  @OneToMany(cascade = { CascadeType.ALL })
  private Set<Account> accounts;

  public long getId() {
    return id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public int getClientId() {
    return clientId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public String getSsn() {
    return ssn;
  }

  public String getSocialInsurancenum() {
    return socialInsurancenum;
  }

  public String getTin() {
    return tin;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public Address getAddress() {
    return address;
  }

  public Set<Account> getAccounts() {
    return accounts;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public void setClientId(int clientId) {
    this.clientId = clientId;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }

  public void setSocialInsurancenum(String socialInsurancenum) {
    this.socialInsurancenum = socialInsurancenum;
  }

  public void setTin(String tin) {
    this.tin = tin;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public void setAccounts(Set<Account> accounts) {
    this.accounts = accounts;
  }

@Override
public String toString() {
    // This method should only be used for logging purposes, never for HTML output
    // For API responses, use proper JSON serialization with Jackson or Gson
    return "Customer [id=" + id + ", customerId=" + customerId + ", clientId=" + clientId + 
           ", firstName=" + firstName + ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth + 
           ", ssn=" + (ssn != null ? "***REDACTED***" : "null") + 
           ", socialInsurancenum=" + (socialInsurancenum != null ? "***REDACTED***" : "null") + 
           ", tin=" + (tin != null ? "***REDACTED***" : "null") + 
           ", phoneNumber=" + phoneNumber + ", address=" + address + ", accounts=" + accounts + "]";
}


}
