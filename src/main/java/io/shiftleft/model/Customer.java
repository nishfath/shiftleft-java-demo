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
    
    // Sanitize all string inputs to prevent XSS and injection attacks
    this.clientId = clientId;
    this.customerId = sanitizeInput(customerId);
    this.firstName = sanitizeInput(firstName);
    this.lastName = sanitizeInput(lastName);
    this.dateOfBirth = dateOfBirth;
    this.ssn = sanitizeInput(ssn);
    this.socialInsurancenum = sanitizeInput(socialInsurancenum);
    this.tin = sanitizeInput(tin);
    this.phoneNumber = sanitizeInput(phoneNumber);
    this.address = address;
    this.accounts = accounts;
}

// Helper method to sanitize inputs
private String sanitizeInput(String input) {
    if (input == null) {
        return null;
    }
    // Remove any HTML/script tags and escape special characters
    return StringEscapeUtils.escapeHtml4(input.trim());
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
    // This method should not be used for HTML output to prevent XSS
    // All fields are HTML-escaped for safety
    return "Customer [id=" + StringEscapeUtils.escapeHtml4(String.valueOf(id)) + 
           ", customerId=" + StringEscapeUtils.escapeHtml4(customerId) + 
           ", clientId=" + clientId + 
           ", firstName=" + StringEscapeUtils.escapeHtml4(firstName) +
           ", lastName=" + StringEscapeUtils.escapeHtml4(lastName) + 
           ", dateOfBirth=" + (dateOfBirth != null ? StringEscapeUtils.escapeHtml4(dateOfBirth.toString()) : "null") + 
           ", ssn=" + maskSensitiveData(ssn) + 
           ", socialInsurancenum=" + maskSensitiveData(socialInsurancenum) + 
           ", tin=" + maskSensitiveData(tin) + 
           ", phoneNumber=" + StringEscapeUtils.escapeHtml4(phoneNumber) + 
           ", address=" + (address != null ? StringEscapeUtils.escapeHtml4(address.toString()) : "null") + 
           ", accounts=" + (accounts != null ? StringEscapeUtils.escapeHtml4(accounts.toString()) : "null") + "]";
}

// Helper method to mask sensitive data in logs and output
private String maskSensitiveData(String data) {
    if (data == null || data.length() < 4) {
        return "****";
    }
    // Show only last 4 characters for sensitive data
    return "****" + StringEscapeUtils.escapeHtml4(data.substring(data.length() - 4));
}


}
