package org.springframework.samples.petclinic;

import org.springframework.samples.petclinic.model.Question;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DemoController {
    private final OwnerRepository ownerRepository;

    private final Question question = new Question();

    public DemoController(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @PostConstruct
    public void init() {
        embedOwners();
    }

    private void embedOwners() {
        List<Owner> owners = ownerRepository.findAll();

        for (Owner owner : owners) {
            String ownerDetails = "Owner name: " + owner.getFirstName() + " " + owner.getLastName() + ", " +
                    "Address: " + owner.getAddress() + ", " +
                    "City: " + owner.getCity() + ", " +
                    "Telephone: " + owner.getTelephone() + ", " +
                    "Pets: " + owner.getPets().stream().map(Pet::getName).collect(Collectors.joining(", "));
            System.out.println(ownerDetails);
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

    @GetMapping("/aiChat/ask")
    String aiChatAsk(@ModelAttribute("question") Question question) {
        //question.setAnswer(assistant.chat(question.getQuestion()));
        return "redirect:/aiChat";
    }

}
