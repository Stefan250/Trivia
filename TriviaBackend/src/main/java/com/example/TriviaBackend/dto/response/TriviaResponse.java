package com.example.TriviaBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class TriviaResponse implements Serializable {
    @JsonProperty("response_code")
    private int responseCode;
    @JsonProperty("results")
    private List<TriviaQuestion> results;

    public TriviaResponse(int responseCode, List<TriviaQuestion> results) {
        this.responseCode = responseCode;
        this.results = results;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public List<TriviaQuestion> getResults() {
        return results;
    }

    public record TriviaQuestion(
            @JsonProperty("type") String type,
            @JsonProperty("difficulty") String difficulty,
            @JsonProperty("category") String category,
            @JsonProperty("question") String question,
            @JsonProperty("correct_answer") String correctAnswer,
            @JsonProperty("incorrect_answers") List<String> incorrectAnswers) {}
}
