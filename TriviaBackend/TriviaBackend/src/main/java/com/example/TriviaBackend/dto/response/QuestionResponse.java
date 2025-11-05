package com.example.TriviaBackend.dto.response;

import java.util.List;

public record QuestionResponse (long id, String question, List<String> answerOptions) {
}
