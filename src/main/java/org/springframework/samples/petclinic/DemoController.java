package org.springframework.samples.petclinic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.petclinic.model.IngestData;
import org.springframework.samples.petclinic.model.Question;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final OwnerRepository ownerRepository;

    private final RestTemplate restTemplate;

    @Value("${petclinic.ai.host}")
    private String aiHost;

    private final Question question = new Question();

    public DemoController(OwnerRepository ownerRepository, RestTemplate restTemplate) {
        this.ownerRepository = ownerRepository;
        this.restTemplate = restTemplate;
    }

    private void embedOwners() {
        List<Owner> owners = ownerRepository.findAll();

        for (Owner owner : owners) {
            String ownerDetails = "Owner name: " + owner.getFirstName() + " " + owner.getLastName() + ", " +
                    "Address: " + owner.getAddress() + ", " +
                    "City: " + owner.getCity() + ", " +
                    "Telephone: " + owner.getTelephone() + ", " +
                    "Pets: " + owner.getPets().stream().map(Pet::getName).collect(Collectors.joining(", "));
            log.info(ownerDetails);
            IngestData data = new IngestData();
            data.setText(ownerDetails);
            restTemplate.postForObject(aiHost + "/ingest", data, Void.class);
        }
    }

    @ModelAttribute("question")
    public Question questionInModel() {
        return question;
    }

    @GetMapping("/aiChat")
    String aiChat(Model model) {
        model.addAttribute("answer", question.getAnswer());
        return "aiChat";
    }

    @GetMapping("/aiChat/ingest")
    String aiChatIngest() {
        embedOwners();
        return "redirect:/aiChat";
    }

    @GetMapping("/aiChat/ask")
    String aiChatAsk(@ModelAttribute("question") Question question) {
        Question ret = restTemplate.postForObject(aiHost + "/ask", question, Question.class);
        assert ret != null;

        question.setAnswer(ret.getAnswer());
        return "redirect:/aiChat";
    }

}
