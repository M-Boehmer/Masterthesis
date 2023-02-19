package com.example.worker6;

import com.example.core.models.*;
import com.example.core.services.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

@RestController
public class FrontendAdapter {

    @Autowired
    KafkaService kafkaService = new KafkaService();

    @Autowired
    PractitionerService practitionerService = new PractitionerService();

    @Autowired
    PatientRecordService patientRecordService = new PatientRecordService();

    @Autowired
    PatientService patientService = new PatientService();

    @Autowired
    ClearanceService clearanceService = new ClearanceService();

    @Autowired
    VaccinationService vaccinationService = new VaccinationService();

     public Map<String,String> replyMap = new HashMap<>();
     public  List<Practitioner> practitionerList = new ArrayList<>();
     public List<Patient> patientList = new ArrayList<>();
     //public List<PatientRecord> patientRecordList = new ArrayList<>();
     public Integer globalMaxID = 0;
     boolean withReply = true;

    @EventListener(ApplicationReadyEvent.class)
    public void refreshPatientAndPractitionerList(){
        practitionerList = practitionerService.getAllPractitioner();
        patientList = patientService.getAllPatients();
    }


    public String waitForReply(String idToWaitFor, Map<String,String> replyMap) {
        await().atMost(180, TimeUnit.SECONDS).until(() -> replyMap.containsKey(idToWaitFor));
        return replyMap.getOrDefault(idToWaitFor,"Error: Got no Reply");
    }

    @KafkaListener(topics = "Replies", groupId = "replier-group")
    public void getReplyFromKafka(Reply reply){
        //System.out.println("ID: "+reply.getId()+"\nReply: "+ reply.getReply());
        replyMap.put(reply.getId(), reply.getReply());
    }

    @GetMapping("/randomRequest")
    @ResponseBody
    public String randomRequests(@RequestParam(value = "type",defaultValue = "") String type){

        List<Patient> patientArrayList = new ArrayList<>(patientList);
        List<PatientRecord>  patientRecordList;
        List<Clearance> clearanceList;
        List<Practitioner> practitionerArrayList = new ArrayList<>(practitionerList);
        String traceID = UUID.randomUUID().toString();
        Patient patient = new Patient();
        Integer patientListIndex = ThreadLocalRandom.current().nextInt(0, patientArrayList.size());
        Integer practitionerListIndex = ThreadLocalRandom.current().nextInt(0, practitionerArrayList.size());

        if (patientArrayList.size() > 0 ) {
            globalMaxID = patientList.size();
            patient = patientArrayList.get(patientListIndex);
        }
        Request request = new Request();
        String reply;
        Practitioner practitioner = practitionerArrayList.get(practitionerListIndex);
        WeightedRandom<String> weightedRandom = new WeightedRandom<String>() //Überprüfung auf variable body im Browser um entweder komplett random oder einzelne Aktion
                .add(25,"GrantAccess")
                .add(3,"RemoveAccess")
                .add(65,"ReadData")
                .add(20,"CreateData")
                .add(3, "UpdateData")
                .add(2, "DeleteData");
        String action = weightedRandom.next();
        request.setId(traceID);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (!type.isEmpty()){
            action = type;
        }
        switch (action){

            case "GrantAccess": // Grant Access Random Request
                int requestedAccessPeriod = ThreadLocalRandom.current().nextInt(0,36+1);
                request.setRequestType("Vaccination_passport");
                request.setRequestedAccessPeriod(Integer.toString(requestedAccessPeriod));
                request.setAction("GrantAccess");
                request.setRequesterID(patient.getPatientID());
                request.setPatientID(patient.getPatientID());
                request.setRequester("Patient"); // z.b mal auf Practitioner
                request.setRequestTable("Clearance");
                request.setPractitionerID(practitioner.getPractitionerID());
                kafkaService.sendMessage(request,"Requests");
                if (withReply) {
                    reply = waitForReply(request.getId(), replyMap);

                    return "Send random request for " + request.getAction() + " with variables: " +
                            "\n Requested Access Period: " + request.getRequestedAccessPeriod() +
                            "\n RequesterID: " + request.getRequesterID() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID: " + request.getPractitionerID() +
                            "\n\n Reply: " + reply;
                } else {
                    return "Send random request for " + request.getAction() + " with variables: " +
                            "\n Requested Access Period: " + request.getRequestedAccessPeriod() +
                            "\n RequesterID: " + request.getRequesterID() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID: " + request.getPractitionerID() +
                            "\n\n Reply: ok";
                }
            case "RemoveAccess": // Remove Access Random Request
                request.setRequestType("Vaccination_passport");
                clearanceList = clearanceService.getAllClearanceByPatientID(patient.getPatientID());
                request.setAction("RemoveAccess");
                request.setRequester("Patient");
                request.setRequestTable("Clearance");
                request.setRequesterID(patient.getPatientID());
                request.setPatientID(patient.getPatientID());
                if (clearanceList.size() > 0){
                    request.setPractitionerID(clearanceList.get(ThreadLocalRandom.current().nextInt(0, clearanceList.size())).getPractitionerID());
                } else {
                    request.setPractitionerID(practitioner.getPractitionerID());
                }
                kafkaService.sendMessage(request,"Requests");
                if (withReply) {
                    reply = waitForReply(request.getId(), replyMap);

                    return "Send random request for " + request.getAction() + " with variables: " +
                            "\n RequesterID: " + request.getRequesterID() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID" + request.getPractitionerID() +
                            "\n RequestType: " + request.getRequestType() +
                            "\n\n Reply: " + reply;
                }else{
                    return "Send random request for " + request.getAction() + " with variables: " +
                            "\n RequesterID: " + request.getRequesterID() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID" + request.getPractitionerID() +
                            "\n RequestType: " + request.getRequestType() +
                            "\n\n Reply: ok";

                }

            case "ReadData":
                request.setRequestType("Vaccination_passport");
                weightedRandom.map.clear();
                weightedRandom.total = 0;
                weightedRandom
                        .add(5,"Clearance")
                        .add(10,"Patient")
                        .add(45,"Practitioner")
                        .add(40,"Vaccination");
                request.setRequestTable(weightedRandom.next());
                weightedRandom.map.clear();
                weightedRandom.total = 0;
                weightedRandom
                        .add(20,"Patient")
                        .add(80,"Practitioner");
                request.setRequester(weightedRandom.next());
                if (request.getRequester().equals("Patient")){
                    request.setRequesterID(patient.getPatientID());
                }else{
                    request.setRequesterID(practitioner.getPractitionerID());
                }
                if (Objects.equals(request.getRequestTable(), "Clearance") || Objects.equals(request.getRequestTable(), "Vaccination") && ThreadLocalRandom.current().nextInt(0, 1) == 0) {
                    request.setAction("FindAllByID");
                }else{
                    request.setAction("FindByID");
                }
                request.setPatientID(patient.getPatientID());
                request.setPractitionerID(practitioner.getPractitionerID());
                kafkaService.sendMessage(request,"Requests");
                if (withReply) {
                    reply = waitForReply(request.getId(),replyMap);
                    return "Send random request for "+ request.getAction() + " with variables: " +
                            "\n RequesterID: " +request.getRequesterID() +
                            "\n Requester: " + request.getRequester() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID: " + request.getPractitionerID()+
                            "\n RequestTable: "+request.getRequestTable()+
                            //"\n\n Reply: ok";
                            "\n\n Reply: "+ reply;
                }else {
                    return "Send random request for "+ request.getAction() + " with variables: " +
                            "\n RequesterID: " +request.getRequesterID() +
                            "\n Requester: " + request.getRequester() +
                            "\n PatientID: " + request.getPatientID() +
                            "\n PractitionerID: " + request.getPractitionerID()+
                            "\n RequestTable: "+request.getRequestTable()+
                            "\n\n Reply: ok";

                }


            case "CreateData":
                Faker randomData = new Faker(Locale.GERMAN);
                request.setRequestType("Vaccination_passport");
                weightedRandom.map.clear();
                weightedRandom.total = 0;
                weightedRandom
                        .add(5,"Patient")
                        .add(95,"Vaccination");
                request.setAction("Create");
                request.setRequester("Practitioner");
                request.setRequesterID(practitioner.getPractitionerID());
                request.setPractitionerID(practitioner.getPractitionerID());
                request.setRequestTable(weightedRandom.next());
                if (Objects.equals(request.getRequestTable(), "Patient")){
                    globalMaxID++;
                    patient.setPatientID(String.valueOf(globalMaxID));
                    patient.setBirthday(randomData.date().birthday(1,99,"dd.MM.YYYY"));
                    patient.setName(randomData.name().firstName());
                    patient.setSurname(randomData.name().lastName());
                    patient.setEmail(patient.getName()+"@"+patient.getSurname()+"."+randomData.internet().domainSuffix());
                    patient.setPhoneNumber(randomData.phoneNumber().phoneNumber());
                    patient.setCity(randomData.address().city());
                    patient.setStreet(randomData.address().streetAddress());
                    patient.setPostalCode(randomData.address().zipCode());
                    request.setPatient(patient);
                    request.setPatientID(patient.getPatientID());
                    patientArrayList.add(patient);
                    patientList = patientArrayList;
                    kafkaService.sendMessage(request,"Requests");
                    if (withReply) {
                        reply = waitForReply(request.getId(), replyMap);
                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID: " + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Patient: " +
                                "\n\t Surname: " + patient.getSurname() +
                                "\n\t Name: " + patient.getName() +
                                "\n\t Birthday: " + patient.getBirthday() +
                                "\n\t Email: " + patient.getEmail() +
                                "\n\t Phone Number: " + patient.getPhoneNumber() +
                                "\n\t City: " + patient.getCity() +
                                "\n\t Street: " + patient.getStreet() +
                                "\n\t Postal Code: " + patient.getPostalCode() +
                                "\n\n Reply: " + reply;
                    }else {
                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID: " + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Patient: " +
                                "\n\t Surname: " + patient.getSurname() +
                                "\n\t Name: " + patient.getName() +
                                "\n\t Birthday: " + patient.getBirthday() +
                                "\n\t Email: " + patient.getEmail() +
                                "\n\t Phone Number: " + patient.getPhoneNumber() +
                                "\n\t City: " + patient.getCity() +
                                "\n\t Street: " + patient.getStreet() +
                                "\n\t Postal Code: " + patient.getPostalCode() +
                                "\n\n Reply: ok";
                    }
                } else {
                    patientRecordList = patientRecordService.getAllPatientRecordByPractitionerID(practitioner.getPractitionerID());
                    if(patientRecordList.size() >= 1){
                        String randomID = patientRecordList.get(ThreadLocalRandom.current().nextInt(0, patientRecordList.size())).getPatientID();
                        patientArrayList = patientArrayList.stream().filter(p -> p.getPatientID().equals(randomID)).collect(Collectors.toList());
                        if(patientArrayList.size() >= 1){
                            patient = patientArrayList.get(ThreadLocalRandom.current().nextInt(0, patientArrayList.size()));
                        }
                    }
                    List<Vaccination> vaccinationList = vaccinationService.getAllVaccinationByPatientID(patient.getPatientID());
                    int maxVaccinationID = 0;
                    request.setPatientID(patient.getPatientID());
                    if(vaccinationList.size()> 0) {
                        maxVaccinationID = Integer.parseInt(Objects.requireNonNull(vaccinationList.stream().max(Comparator.comparing(Vaccination::getVaccinationID)).get()).getVaccinationID());
                    }
                    Vaccination.CompositeKey compositeKey = new Vaccination.CompositeKey(patient.getPatientID(),String.valueOf(maxVaccinationID + 1 ));
                    List<String> manufacturers = Stream.of("Moderna","Pfizer–BioNTech","Janssen Pharmaceutica","Oxford–AstraZeneca").toList();
                    int minDay = (int) LocalDate.of(2022, 1, 1).toEpochDay();
                    int maxDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
                    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
                    LocalDate date = LocalDate.ofEpochDay(randomDay);
                    //String localDate = date.format(formatter);
                    Vaccination vaccination = new Vaccination(
                            compositeKey,
                            date.toString(),
                            manufacturers.get(ThreadLocalRandom.current().nextInt(0, 3)),
                            randomData.expression("#{bothify '??####', 'true'}"),
                            practitioner.getPractitionerID(),
                            "COVID-19");
                    request.setVaccination(vaccination);
                    kafkaService.sendMessage(request,"Requests");
                    if (withReply) {
                        reply = waitForReply(request.getId(), replyMap);

                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID: " + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Vaccination: " +
                                "\n\t Date: " + vaccination.getDate() +
                                "\n\t Manufacturer: " + vaccination.getManufacturer() +
                                "\n\t Batch Nr: " + vaccination.getBatchNr() +
                                "\n\t VaccinationAgainst: " + vaccination.getVaccinationAgainst() +
                                //"\n\n Reply: ok";
                                "\n\n Reply: " + reply;
                    }else {
                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID: " + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Vaccination: " +
                                "\n\t Date: " + vaccination.getDate() +
                                "\n\t Manufacturer: " + vaccination.getManufacturer() +
                                "\n\t Batch Nr: " + vaccination.getBatchNr() +
                                "\n\t VaccinationAgainst: " + vaccination.getVaccinationAgainst() +
                                "\n\n Reply: ok";
                    }
                }
            case "UpdateData":

                Faker randomData2 = new Faker(Locale.GERMAN);
                request.setRequestType("Vaccination_passport");
                weightedRandom.map.clear();
                weightedRandom.total = 0;
                weightedRandom
                        .add(70,"Patient")
                        .add(1,"Vaccination")
                        .add(29,"Practitioner");
                request.setAction("Update");
                request.setRequester("Practitioner");
                request.setRequesterID(practitioner.getPractitionerID());
                request.setPractitionerID(practitioner.getPractitionerID());
                request.setPatientID(patient.getPatientID());
                request.setRequestTable(weightedRandom.next());
                switch (request.getRequestTable()) {
                    case "Patient" -> {
                        patient.setBirthday(randomData2.date().birthday(1, 99, "dd.MM.YYYY"));
                        patient.setName(randomData2.name().firstName());
                        patient.setSurname(randomData2.name().lastName());
                        patient.setEmail(patient.getName() + "@" + patient.getSurname() + "." + randomData2.internet().domainSuffix());
                        patient.setPhoneNumber(randomData2.phoneNumber().phoneNumber());
                        patient.setCity(randomData2.address().city());
                        patient.setStreet(randomData2.address().streetAddress());
                        patient.setPostalCode(randomData2.address().zipCode());
                        request.setPatient(patient);
                        patientArrayList.set(patientListIndex,patient);
                        patientList = patientArrayList;
                        kafkaService.sendMessage(request, "Requests");
                        if (withReply) {
                            reply = waitForReply(request.getId(), replyMap);
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID" + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Patient: " +
                                    "\n\t Surname: " + patient.getSurname() +
                                    "\n\t Name: " + patient.getName() +
                                    "\n\t Birthday: " + patient.getBirthday() +
                                    "\n\t Email: " + patient.getEmail() +
                                    "\n\t Phone Number: " + patient.getPhoneNumber() +
                                    "\n\t City: " + patient.getCity() +
                                    "\n\t Street: " + patient.getStreet() +
                                    "\n\t Postal Code: " + patient.getPostalCode() +
                                    //"\n\n Reply: ok";
                                    "\n\n Reply: " + reply;
                        } else {
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID" + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Patient: " +
                                    "\n\t Surname: " + patient.getSurname() +
                                    "\n\t Name: " + patient.getName() +
                                    "\n\t Birthday: " + patient.getBirthday() +
                                    "\n\t Email: " + patient.getEmail() +
                                    "\n\t Phone Number: " + patient.getPhoneNumber() +
                                    "\n\t City: " + patient.getCity() +
                                    "\n\t Street: " + patient.getStreet() +
                                    "\n\t Postal Code: " + patient.getPostalCode() +
                                    "\n\n Reply: ok";
                        }
                    }
                    case "Vaccination" -> {
                        List<Vaccination> vaccinationList = vaccinationService.getAllVaccinationByPatientID(patient.getPatientID());
                        List<String> manufacturers = Stream.of("Moderna", "Pfizer–BioNTech", "Janssen Pharmaceutica", "Oxford–AstraZeneca").toList();
                        int minDay = (int) LocalDate.of(2022, 1, 1).toEpochDay();
                        int maxDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
                        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
                        LocalDate date = LocalDate.ofEpochDay(randomDay);
                        //String s = date.format(formatter);
                        //LocalDate localDate = LocalDate.parse(s,formatter);
                        Vaccination vaccination = vaccinationList.get(ThreadLocalRandom.current().nextInt(0, vaccinationList.size()));
                        vaccination.setDate(date.toString());
                        vaccination.setBatchNr(randomData2.expression("#{bothify '??####', true}"));
                        vaccination.setManufacturer(manufacturers.get(ThreadLocalRandom.current().nextInt(0, 3)));
                        request.setVaccination(vaccination);
                        kafkaService.sendMessage(request, "Requests");
                        if (withReply) {
                            reply = waitForReply(request.getId(), replyMap);
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID: " + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Vaccination: " +
                                    "\n\t Date: " + vaccination.getDate() +
                                    "\n\t Manufacturer: " + vaccination.getManufacturer() +
                                    "\n\t Batch Nr: " + vaccination.getBatchNr() +
                                    "\n\t VaccinationAgainst: " + vaccination.getVaccinationAgainst() +
                                    //"\n\n Reply: ok";
                                    "\n\n Reply: " + reply;
                        } else {
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID: " + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Vaccination: " +
                                    "\n\t Date: " + vaccination.getDate() +
                                    "\n\t Manufacturer: " + vaccination.getManufacturer() +
                                    "\n\t Batch Nr: " + vaccination.getBatchNr() +
                                    "\n\t VaccinationAgainst: " + vaccination.getVaccinationAgainst() +
                                    "\n\n Reply: ok";
                        }
                    }
                    case "Practitioner" -> {
                        practitioner.setName(randomData2.name().firstName());
                        practitioner.setSurname(randomData2.name().lastName());
                        practitioner.setPhoneNumber(randomData2.phoneNumber().phoneNumber());
                        practitioner.setCity(randomData2.address().city());
                        practitioner.setStreet(randomData2.address().streetAddress());
                        practitioner.setPostalCode(randomData2.address().zipCode());
                        practitioner.setEmail(practitioner.getName() + "@" + practitioner.getSurname() + "." + randomData2.internet().domainSuffix());
                        request.setPractitioner(practitioner);
                        practitionerArrayList.set(practitionerListIndex,practitioner);
                        practitionerList = practitionerArrayList;
                        kafkaService.sendMessage(request, "Requests");
                        if (withReply) {
                            reply = waitForReply(request.getId(), replyMap);
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID" + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Practitioner: " +
                                    "\n\t Surname: " + practitioner.getSurname() +
                                    "\n\t Name: " + practitioner.getName() +
                                    "\n\t Email: " + practitioner.getEmail() +
                                    "\n\t Phone Number: " + practitioner.getPhoneNumber() +
                                    "\n\t City: " + practitioner.getCity() +
                                    "\n\t Street: " + practitioner.getStreet() +
                                    "\n\t Postal Code: " + practitioner.getPostalCode() +
                                    //"\n\n Reply: ok";
                                    "\n\n Reply: " + reply;
                        } else {
                            return "Send random request for " + request.getAction() + " with variables: " +
                                    "\n RequesterID: " + request.getRequesterID() +
                                    "\n Requester: " + request.getRequester() +
                                    "\n PatientID: " + request.getPatientID() +
                                    "\n PractitionerID" + request.getPractitionerID() +
                                    "\n RequestTable: " + request.getRequestTable() +
                                    "\n Practitioner: " +
                                    "\n\t Surname: " + practitioner.getSurname() +
                                    "\n\t Name: " + practitioner.getName() +
                                    "\n\t Email: " + practitioner.getEmail() +
                                    "\n\t Phone Number: " + practitioner.getPhoneNumber() +
                                    "\n\t City: " + practitioner.getCity() +
                                    "\n\t Street: " + practitioner.getStreet() +
                                    "\n\t Postal Code: " + practitioner.getPostalCode() +
                                    "\n\n Reply: ok";
                        }
                    }
                }
            case "DeleteData":
                request.setRequestType("Vaccination_passport");
                weightedRandom.map.clear();
                weightedRandom.total = 0;
                weightedRandom
                        .add(99,"Patient")
                        .add(1,"Vaccination");
                request.setAction("Delete");
                request.setRequester("Practitioner");
                request.setRequesterID(practitioner.getPractitionerID());
                request.setPractitionerID(practitioner.getPractitionerID());
                request.setPatientID(patient.getPatientID());
                request.setRequestTable(weightedRandom.next());
                if(request.getRequestTable().equals("Patient")){
                    patientArrayList.remove(patientListIndex);
                    patientList = patientArrayList;
                    kafkaService.sendMessage(request,"Requests");
                    if (withReply) {
                        reply = waitForReply(request.getId(), replyMap);

                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID" + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                //"\n\n Reply: ok";
                                "\n\n Reply: " + reply;
                    }else {
                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID" + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n\n Reply: ok";
                    }
                } else {
                    List<Vaccination> vaccinationList = vaccinationService.getAllVaccinationByPatientID(patient.getPatientID());
                    request.setVaccination(vaccinationList.get(ThreadLocalRandom.current().nextInt(0, vaccinationList.size())));

                    kafkaService.sendMessage(request,"Requests");
                    if (withReply) {
                        reply = waitForReply(request.getId(), replyMap);

                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID" + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Vaccination: " +
                                "\n\t VaccinationID: " + request.getVaccination().getVaccinationID() +
                                //"\n\n Reply: ok";
                                "\n\n Reply: " + reply;
                    } else {
                        return "Send random request for " + request.getAction() + " with variables: " +
                                "\n RequesterID: " + request.getRequesterID() +
                                "\n Requester: " + request.getRequester() +
                                "\n PatientID: " + request.getPatientID() +
                                "\n PractitionerID" + request.getPractitionerID() +
                                "\n RequestTable: " + request.getRequestTable() +
                                "\n Vaccination: " +
                                "\n\t VaccinationID: " + request.getVaccination().getVaccinationID() +
                                "\n\n Reply: ok";
                    }
                }
        }
    return "Error, something went wrong";
    }


    public static class WeightedRandom<E> {
        private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
        private final Random random;
        private double total = 0;

        public WeightedRandom() {
            this(new Random());
        }

        public WeightedRandom(Random random) {
            this.random = random;
        }

        public WeightedRandom<E> add(double weight, E result) {
            if (weight <= 0) return this;
            total += weight;
            map.put(total, result);
            return this;
        }

        public E next() {
            double value = random.nextDouble() * total;
            return map.higherEntry(value).getValue();
        }
    }

}
