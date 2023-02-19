package org.example.database;

// snippet-start:[dynamodb.java2.get_item.import]
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import java.util.HashMap;
import java.util.Map;

public class GetItem {

    public static Map<String,AttributeValue> main(String[] args) {


        if (args.length != 3) {
            System.exit(1);
        }

        String tableName = args[0];
        String key = args[1];
        String keyVal = args[2];
        System.out.format("Retrieving item \"%s\" from \"%s\"\n", keyVal, tableName);

        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("dynamo");
        Region region = Region.EU_CENTRAL_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();

        Map<String,AttributeValue> returnValue = getDynamoDBItem(ddb, tableName, key, keyVal);
        ddb.close();
        return returnValue;
    }


    public static Map<String,AttributeValue> getDynamoDBItem(DynamoDbClient ddb,String tableName,String key,String keyVal ) {

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();

        Map<String, AttributeValue> returnedItem = null;
        try {
            returnedItem = ddb.getItem(request).item();
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return returnedItem;
    }
}