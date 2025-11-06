package com.example.TriviaBackend.client;

import com.example.TriviaBackend.dto.response.TriviaResponse;
import com.example.TriviaBackend.entity.QuestionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TriviaClient {
    private final Logger LOG = LoggerFactory.getLogger(TriviaClient.class);
    private final String URI = "https://opentdb.com/api.php?amount=10";
    private final RestTemplate restTemplate;

    @Autowired
    public TriviaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<QuestionEntity> getQuestions() {
        final TriviaResponse response = restTemplate.getForObject(URI, TriviaResponse.class);

        if (response == null) {
            return List.of();
        }

        return switch (response.getResponseCode()) {
            // case 1 - 4 will return an empty list, since these are errors based on parameters we don't
            // use
            case 0 -> {
                LOG.info("Retrieved {} questions from Trivia API", response.getResults().size());
                yield transfigureTriviaResponse(response.getResults());
            }
            case 5 -> {
                LOG.error("Rate limit of the Open Trivia API reached, throwing exception");
                throw new RuntimeException("Rate limit reached");
            }
            default -> {
                LOG.error("Open Trivia API returned an empty list");
                yield List.of();
            }
        };
    }

    private List<QuestionEntity> transfigureTriviaResponse(
            List<TriviaResponse.TriviaQuestion> results) {
        return results.stream().map(QuestionEntity::new).toList();
    }
}
