package org.example.worker;

import org.example.database.GetItem;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>When connecting to a cluster in Camunda Cloud, this application assumes that the following
 * environment variables are set:
 *
 * <ul>
 *   <li>ZEEBE_ADDRESS
 *   <li>ZEEBE_CLIENT_ID
 *   <li>ZEEBE_CLIENT_SECRET
 *   <li>ZEEBE_AUTHORIZATION_SERVER_URL
 * </ul>
 *
 * <p><strong>Hint:</strong> When you create client credentials in Camunda Cloud you have the option
 * to download a file with above lines filled out for you.
 *
 * <p>When {@code ZEEBE_ADDRESS} is not set, it connects to a broker running on localhost with
 * default ports
 */
public final class Check_access_validity_Worker {
    public static void main(final String[] args) {
        final String defaultAddress = "localhost:26500";
        final ZeebeClientBuilder clientBuilder;
        clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();

        final String jobType = "Check_access_validity";

        try (final ZeebeClient client = clientBuilder.build()) {

            try (final JobWorker workerRegistration =
                         client
                                 .newWorker()
                                 .jobType(jobType)
                                 .handler(new Handler())
                                 .timeout(Duration.ofSeconds(10))
                                 .open()) {
                System.out.println("Job worker opened and receiving jobs.");

                waitUntilSystemInput("exit");
            }
        }
    }

    private static class Handler implements JobHandler {
        @Override
        public void handle(final JobClient client, final ActivatedJob job) {
            final Map<String, Object> variables = job.getVariablesAsMap();
            String practitionerID = variables.get("PractitionerID").toString();
            String requested_access = variables.get("Requested_Access").toString();
            String[] request = new String[3];
            request[0]="Practitioner";
            request[1]="PractitionerID";
            request[2]=practitionerID;
            final Map<String, Object> result = new HashMap<>();
            Map<String, AttributeValue> returnItem = GetItem.main(request);

            if (!returnItem.containsKey("Medical Speciality")){
                result.put("Request_validity","Not_valid");
                client.newCompleteCommand(job.getKey())
                        .variables(result)
                        .send()
                        .join();
            }else{
                String speciality = returnItem.get("Medical Speciality").s();

                switch (requested_access){
                    //For now only the Vaccination Access request is implemented because of the scope of the Thesis.
                    //Other Functions can be added by adding additional cases.
                    case "Vaccination_passport":
                        if (speciality.equals("Family Doctor") || speciality.equals("Emergency Doctor") || speciality.equals("Hospital Doctor")){
                            result.put("Request_validity","Valid");
                        }else result.put("Request_validity","Not_valid");
                        break;
                    default:
                        result.put("Request_validity","Not_valid");
                        break;
                }
                client.newCompleteCommand(job.getKey())
                        .variables(result)
                        .send()
                        .join();
            }
        }
    }


    private static void waitUntilSystemInput(final String exitCode) {
        try (final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String nextLine = scanner.nextLine();
                if (nextLine.contains(exitCode)) {
                    return;
                }
            }
        }
    }
}
