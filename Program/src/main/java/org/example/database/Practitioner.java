package org.example.database;


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;



@DynamoDbBean
public class Practitioner {

    private String PractitionerID;
    private String Name;
    private String Surname;
    private String Medical_speciality;
    private String Street;
    private String City;
    private String Postal_code;
    private String Phone_number;
    private String Email;

    @DynamoDbPartitionKey
    public String getPractitionerID() {return this.PractitionerID;};
    public void setPractitionerID(String practitionerID) {PractitionerID = practitionerID;}


    public String getEmail() {return Email;}
    public void setEmail(String email) {Email = email;}


    public String getName() {return Name;}
    public void setName(String name) {Name = name;}


    public String getSurname() {return Surname;}
    public void setSurname(String surname) {Surname = surname;}


    public String getMedical_speciality() {return Medical_speciality;}
    public void setMedical_speciality(String medical_speciality) {Medical_speciality = medical_speciality;}


    public String getStreet() {return Street;}
    public void setStreet(String street) {Street = street;}


    public String getCity() {return City;}
    public void setCity(String city) {City = city;}


    public String getPostal_code() {return Postal_code;}
    public void setPostal_code(String postal_code) {Postal_code = postal_code;}


    public String getPhone_number() {return Phone_number;}
    public void setPhone_number(String phone_number) {Phone_number = phone_number;}

    public static Practitioner getItem(DynamoDbEnhancedClient enhancedClient,Integer PractitionerID) {

        Practitioner result = null;

        try {
            DynamoDbTable<Practitioner> table = enhancedClient.table("Practitioner", TableSchema.fromBean(Practitioner.class));
            Key key = Key.builder()
                    .partitionValue("2")
                    .build();

            // Get the item by using the key.
            result = table.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            System.out.println("******* The description value is " + result.getMedical_speciality());

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return result;
    }
}
