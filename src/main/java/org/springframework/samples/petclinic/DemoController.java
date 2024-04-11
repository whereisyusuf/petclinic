package org.springframework.samples.petclinic;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.samples.petclinic.model.Question;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DemoController {
    private final ChatLanguageModel chatLanguageModel;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final OwnerRepository ownerRepository;

    private ConversationalChain chain = null;
    private final Question question = new Question();
    private final Question searchQuestion = new Question();

    public DemoController(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, OwnerRepository ownerRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.ownerRepository = ownerRepository;
        embedOwners();
        initConversationalChain();
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

    private void initConversationalChain() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
        this.chain = ConversationalChain.builder()
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .build();
    }

    @ModelAttribute("question")
    public Question questionInModel() {
        return question;
    }

    @ModelAttribute("searchQuestion")
    public Question searchQuestionInModel() {
        return searchQuestion;
    }

    @GetMapping("/aiChat")
    String aiChat(Model model) {
        model.addAttribute("answer", question.getAnswer());
        model.addAttribute("searchAnswer", searchQuestion.getAnswer());

        System.out.println("question: " + question.getQuestion());
        System.out.println("answer: " + question.getAnswer());
        System.out.println("searchQuestion: " + searchQuestion.getQuestion());
        System.out.println("searchAnswer: " + searchQuestion.getAnswer());

        return "aiChat";
    }

    @GetMapping("/aiChat/ask")
    String aiChatAsk(@ModelAttribute("question") Question question) {
        question.setAnswer(chain.execute(question.getQuestion()));
        return "redirect:/aiChat";
    }

    @GetMapping("/aiChat/search")
    String aiChatSearch(@ModelAttribute("searchQuestion") Question searchQuestion) {
        StringBuilder answer = new StringBuilder();

        Embedding relevantEmbedding = embeddingModel.embed(searchQuestion.getQuestion()).content();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(relevantEmbedding, 10);
        for (EmbeddingMatch<TextSegment> textSegmentEmbeddingMatch : relevant) {
            answer.append(textSegmentEmbeddingMatch.embedded().text()).append(". ");
        }

        searchQuestion.setAnswer(answer.toString());
        return "redirect:/aiChat";
    }
}
