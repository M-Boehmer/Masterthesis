package com.example.core.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Practitioner")
public class Practitioner {

    private String practitionerID;
    private String name;
    private String surname;
    private String medicalSpeciality;
    private String street;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String email;

    @DynamoDBHashKey(attributeName = "PractitionerID")
    public String getPractitionerID() {return this.practitionerID;};
    public void setPractitionerID(String practitionerID) {
        this.practitionerID = practitionerID;}

    @DynamoDBAttribute(attributeName = "Email")
    public String getEmail() {return email;}
    public void setEmail(String email) {
        this.email = email;}

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {return name;}
    public void setName(String name) {
        this.name = name;}

    @DynamoDBAttribute(attributeName = "Surname")
    public String getSurname() {return surname;}
    public void setSurname(String surname) {
        this.surname = surname;}

    @DynamoDBAttribute(attributeName = "Medical speciality")
    public String getMedicalSpeciality() {return medicalSpeciality;}
    public void setMedicalSpeciality(String medicalSpeciality) {
        this.medicalSpeciality = medicalSpeciality;}

    @DynamoDBAttribute(attributeName = "Street")
    public String getStreet() {return street;}
    public void setStreet(String street) {
        this.street = street;}

    @DynamoDBAttribute(attributeName = "City")
    public String getCity() {return city;}
    public void setCity(String city) {
        this.city = city;}

    @DynamoDBAttribute(attributeName = "Postal code")
    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;}

    @DynamoDBAttribute(attributeName = "Phone number")
    public String getPhoneNumber() {return phoneNumber;}
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Practitioner that = (Practitioner) o;
        return Objects.equals(practitionerID, that.practitionerID) && Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(medicalSpeciality, that.medicalSpeciality) && Objects.equals(street, that.street) && Objects.equals(city, that.city) && Objects.equals(postalCode, that.postalCode) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerID, name, surname, medicalSpeciality, street, city, postalCode, phoneNumber, email);
    }
}
