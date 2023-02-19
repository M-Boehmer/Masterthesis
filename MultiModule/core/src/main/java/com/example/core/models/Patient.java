package com.example.core.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Patient")
public class Patient {

    private String patientID;
    private String name;
    private String surname;
    private String birthday;
    private String street;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String email;

    @DynamoDBHashKey(attributeName = "PatientID")
    public String getPatientID() {return patientID;}
    public void setPatientID(String patientID) {
        this.patientID = patientID;}

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {return name;}
    public void setName(String name) {
        this.name = name;}

    @DynamoDBAttribute(attributeName = "Surname")
    public String getSurname() {return surname;}
    public void setSurname(String surname) {
        this.surname = surname;}

    @DynamoDBAttribute(attributeName = "Birthday")
    public String getBirthday() {return birthday;}
    public void setBirthday(String birthday) {
        this.birthday = birthday;}

    @DynamoDBAttribute(attributeName = "Street")
    public String getStreet() {return street;}
    public void setStreet(String street) {
        this.street = street;}

    @DynamoDBAttribute(attributeName = "City")
    public String getCity() {return city;}
    public void setCity(String city) {
        this.city = city;}

    @DynamoDBAttribute(attributeName = "Postal Code")
    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;}

    @DynamoDBAttribute(attributeName = "Phone Number")
    public String getPhoneNumber() {return phoneNumber;}
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;}

    @DynamoDBAttribute(attributeName = "Email")
    public String getEmail() {return email;}
    public void setEmail(String email) {
        this.email = email;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(patientID, patient.patientID) && Objects.equals(name, patient.name) && Objects.equals(surname, patient.surname) && Objects.equals(birthday, patient.birthday) && Objects.equals(street, patient.street) && Objects.equals(city, patient.city) && Objects.equals(postalCode, patient.postalCode) && Objects.equals(phoneNumber, patient.phoneNumber) && Objects.equals(email, patient.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientID, name, surname, birthday, street, city, postalCode, phoneNumber, email);
    }
}
