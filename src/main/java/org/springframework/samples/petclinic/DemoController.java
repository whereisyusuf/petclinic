package org.springframework.samples.petclinic;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Controller
public class DemoController {




    private final ChatLanguageModel chatLanguageModel;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    public DemoController(ImageModel imageModel, ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }


    @GetMapping("/aidemo")
    String loadVectorDatabase(Model model) {
        // replace these value with  owner data
        String content1 = "banana is a fruit";
        String content2 = "computer is ";
        String content3 = "apple";
        String content4 = "pizza";
        String content5 = "strawberry";
        String content6 = "chess";
     
/*1. George Franklin - Address: 110 W. Liberty St., Madison - Telephone: 6085551023 - Pet: Leo, a cat born on 2010-09-07 2. Betty Davis - Address: 638 Cardinal Ave., Sun Prairie - Telephone: 6085551749 - Pet: Basil, a hamster born on 2012-08-06 3. Eduardo Rodriquez - Address: 2693 Commerce St., McFarland - Telephone: 6085558763 - Pets: Jewel and Rosy, both dogs born on 2010-03-07 and 2011-04-17 respectively 4. Harold Davis - Address: 563 Friendly St., Windsor - Telephone: 6085553198 - Pet: Iggy, a lizard born on 2010-11-30 5. Peter McTavish - Address: 2387 S. Fair Way, Madison - Telephone: 6085552765 - Pet: George, a snake born on 2010-01-20 Let me know if you need further information. */
        List<String> contents = asList(content1, content2, content3, content4, content5, content6);

        for (String content : contents) {
            TextSegment textSegment = TextSegment.from(content);
            Embedding embedding = embeddingModel.embed(content).content();
            embeddingStore.add(embedding, textSegment);
        }

        model.addAttribute("demo", "Demo 6: Data ingestion");
        model.addAttribute("question", "Ingesting data into the vector database");
        model.addAttribute("answer", "OK");
 
        String question = "list all owners";

        Embedding relevantEmbedding = embeddingModel.embed(question).content();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(relevantEmbedding, 3);

        String answer = relevant.get(0).embedded().text() + "\n";
        answer += relevant.get(1).embedded().text() + "\n";
        answer += relevant.get(2).embedded().text() + "\n";

        model.addAttribute("demo", "Demo 7: Querying the vector database");
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        return "demo";
    }

}
