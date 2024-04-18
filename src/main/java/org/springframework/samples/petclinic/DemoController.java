package org.springframework.samples.petclinic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.service.AiServices;

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
    private final ChatLanguageModel chatLanguageModel;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final OwnerRepository ownerRepository;


    private Agent assistant = null;
    private final Question question = new Question();
    

    public DemoController(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, OwnerRepository ownerRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.ownerRepository = ownerRepository;
 
    }

    @PostConstruct
    public void init() {
        embedOwners();
        initChain();
    }

    private void embedOwners() {
        List<Owner> owners = ownerRepository.findAll();

        for (Owner owner : owners) {
            String ownerDetails = "Owner name: " + owner.getFirstName() + " " + owner.getLastName() + ", " +
                    "Address: " + owner.getAddress() + ", " +
                    "City: " + owner.getCity() + ", " +
                    "Telephone: " + owner.getTelephone() + ", " +
                    "Pets: " + owner.getPets().stream().map(Pet::getName).collect(Collectors.joining(", "));
            TextSegment textSegment = TextSegment.from(ownerDetails);
            Embedding embedding = embeddingModel.embed(ownerDetails).content();
            embeddingStore.add(embedding, textSegment);

        }
    }

    private void initChain() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        assistant = AiServices.builder(Agent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .build();
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
        question.setAnswer(assistant.chat(question.getQuestion()));
        return "redirect:/aiChat";
    }

}
