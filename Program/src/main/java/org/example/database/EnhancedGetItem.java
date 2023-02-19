package org.example.database;


import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class EnhancedGetItem {

    public static void main(String[] args) throws ClassNotFoundException {

        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("dynamo");
        Region region = Region.EU_CENTRAL_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        Practitioner p = Practitioner.getItem(enhancedClient,2);

        ddb.close();
    }

    // snippet-start:[dynamodb.java2.mapping.getitem.main]

    public static void putRecord(DynamoDbEnhancedClient enhancedClient) {
        try {
            DynamoDbTable<Practitioner> custTable = enhancedClient.table("Customer", TableSchema.fromBean(Practitioner.class));


            // Populate the Table.
            Practitioner PractRecord = new Practitioner();
            PractRecord.setPractitionerID("2");
            PractRecord.setName("Max");
            PractRecord.setSurname("Mustermann");
            //PractRecord.setMedical_speciality("Family Doctor");
            PractRecord.setStreet("Max Mustermann Str.70");
            //PractRecord.setPostal_code("72634");
            //PractRecord.setPhone_number("0627-283910");
            PractRecord.setEmail("Max.mustermann@test.de");

            // Put the customer data into an Amazon DynamoDB table.
            custTable.putItem(PractRecord);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Customer data added to the table with id id101");
    }
    // snippet-end:[dynamodb.java2.mapping.getitem.main]
}
