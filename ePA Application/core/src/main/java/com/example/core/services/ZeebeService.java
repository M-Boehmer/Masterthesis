package com.example.core.services;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Map;

public class ZeebeService {
    public static void zeebeCompleteWithoutVariables(ZeebeClient client, ActivatedJob job, Logger LOG) {
        client.newCompleteCommand(job.getKey())
                .send()
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        LOG.info("Exception occurs");
                        LOG.error(String.valueOf(exception));
                    } else {
                        LOG.info("No exception, got result: " + result);
                    }
                });
    }

    public static void zeebeActivateJob(ZeebeClient client,String processID, Map<String, Object> attributes, Logger LOG){
        client.newCreateInstanceCommand()
                .bpmnProcessId(processID)
                .latestVersion()
                .variables(attributes)
                .requestTimeout(Duration.ofSeconds(30))
                .send()
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        LOG.info("exception occurs");
                        LOG.error(String.valueOf(exception));
                    } else {
                        LOG.info("no exception, got result: " + result);
                    }
                });
    }

    public static void zeebeThrowError(ZeebeClient client, long instanceKey, String Error, Logger LOG){
        LOG.warn(Error);
        Map<String, Object> attributes = Map.of("Error",Error);
        client.newSetVariablesCommand(instanceKey).variables(attributes).send().join();
        throw new ZeebeBpmnError("General_Error",attributes.get("Error").toString());
    }
}
