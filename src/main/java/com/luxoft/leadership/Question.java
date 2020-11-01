package com.luxoft.leadership;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Question {
    public String text;
    public Set<String> options = new HashSet<>();
    public Set<String> answers = new HashSet<>();
    public Set<String> wrongAnswers = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question1 = (Question) o;
        return Objects.equals(text, question1.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    public void wrong() {
        wrongAnswers.addAll(answers);
        answers.clear();
    }
}
