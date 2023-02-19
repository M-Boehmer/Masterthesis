package com.example.core.models;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Vaccination")
public class Vaccination {

    @Id
    @DynamoDBIgnore
    private CompositeKey compositeKey;
    @NonNull
    private String date;
    @NonNull
    private String manufacturer;
    @NonNull
    private String batchNr;
    @NonNull
    private String practitionerID;
    @NonNull
    private String vaccinationAgainst;



    @DynamoDBHashKey(attributeName = "PatientID")
    public String getPatientID() {return compositeKey != null ? compositeKey.getPatientID() : null;}
    public void setPatientID(String patientID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
        }
        compositeKey.setPatientID(patientID);
    }
    @DynamoDBRangeKey(attributeName = "VaccinationID")
    public String getVaccinationID() {return compositeKey != null ? compositeKey.getVaccinationID() : null;}
    public void setVaccinationID(String vaccinationID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
        }
        compositeKey.setVaccinationID(vaccinationID);
    }

    @DynamoDBAttribute(attributeName = "Date")
    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    @DynamoDBAttribute(attributeName = "Manufacturer")
    public String getManufacturer() {return manufacturer;}
    public void setManufacturer(String manufacturer) {this.manufacturer = manufacturer;}

    @DynamoDBAttribute(attributeName = "Batch Nr")
    public String getBatchNr() {return batchNr;}
    public void setBatchNr(String batchNr) {this.batchNr = batchNr;}

    @DynamoDBAttribute(attributeName = "PractitionerID")
    public String getPractitionerID() {return practitionerID;}
    public void setPractitionerID(String practitionerID) {
        this.practitionerID = practitionerID;}

    @DynamoDBAttribute(attributeName = "Vaccination against")
    public String getVaccinationAgainst() {return vaccinationAgainst;}
    public void setVaccinationAgainst(String vaccinationAgainst) {this.vaccinationAgainst = vaccinationAgainst;}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDBDocument
    public static class CompositeKey {
        @DynamoDBHashKey
        @DynamoDBAttribute(attributeName = "PatientID")
        private String patientID;

        @DynamoDBRangeKey
        @DynamoDBAttribute(attributeName = "VaccinationID")
        private String vaccinationID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vaccination that = (Vaccination) o;
        return Objects.equals(compositeKey, that.compositeKey) && date.equals(that.date) && manufacturer.equals(that.manufacturer) && batchNr.equals(that.batchNr) && practitionerID.equals(that.practitionerID) && vaccinationAgainst.equals(that.vaccinationAgainst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compositeKey, date, manufacturer, batchNr, practitionerID, vaccinationAgainst);
    }
}
