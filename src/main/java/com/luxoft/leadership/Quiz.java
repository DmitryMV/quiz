package com.luxoft.leadership;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class Quiz {


    public static final Path QUESTIONS_POOL_FILE = Paths.get("questionsPool.json");

    public static void main(String[] args) throws IOException {
        Quiz quiz = new Quiz();
        quiz.start();
    }

    private final Map<String, Question> questionsPool = new HashMap<>();

    public void start() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            loadKnowledgeBase();
            while (true) {
                System.out.println("New Quiz");
                startQuiz(in);
            }
        }
    }

    public void startQuiz(BufferedReader in) throws IOException {
        String line;
        System.out.println("Enter question followed by empty line or 'end quiz' to review quiz:");
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if ("end quiz".equalsIgnoreCase(line)) {
                review(in);
                saveKnowledgeBase();
                return;
            } else {
                readQuestion(line, in);
            }
            saveKnowledgeBase();
            System.out.println("Enter question followed by empty line or 'end quiz' to review quiz:");
        }
    }

    private void readQuestion(String questionText, BufferedReader in) throws IOException {
        Set<String> options = new LinkedHashSet<>();
        while (questionText.isEmpty()) {
            questionText = in.readLine();
            if (questionText == null) {
                return;
            }
        }
        String line = null;

        while ((line = in.readLine()) != null) {
            line = line.trim();
            if ("".equalsIgnoreCase(line)) {
                break;
            } else {
                options.add(line);
            }
        }
        Question question = questionsPool.computeIfAbsent(questionText, (q) -> new Question());
        question.text = questionText;
        question.options.addAll(options);
        if (!question.answers.isEmpty()) {
            System.out.println("Correct answers:");
            for (String answer : question.answers) {
                System.out.println(format("  %s", answer));
            }
            System.out.println("hit ENTER to proceed or 'change' to change answer");
            line = in.readLine();
            if ("".equalsIgnoreCase(line)) {
                System.out.println();
                return;
            }
        }
        System.out.println("Pick answer:");
        String[] optionsArr = options.toArray(new String[0]);
        int i = 1;
        for (String option : optionsArr) {
            String marker;
            if (question.wrongAnswers.contains(option)) {
                marker = "X";
            } else if (question.answers.contains(option)) {
                marker = "V";
            } else {
                marker = " ";
            }
            System.out.println(format("[%s]  %d: %s", marker, i++, option));
        }
        boolean read = false;
        Set<String> selectedAnswers = null;
        String[] indxes = null;
        do {
            try {
                System.out.print("Answer # (comma separated): ");
                indxes = in.readLine().split(",");
                selectedAnswers = new LinkedHashSet<>();
                for (String indx : indxes) {
                    int idx = Integer.parseInt(indx.trim());
                    String selected = optionsArr[idx - 1];
                    selectedAnswers.add(selected);
                }
                read = true;
            } catch (NumberFormatException e) {
                System.err.println("Failed to read answers. Please repeat.");
            }
        } while (!read);
        System.out.println("You have selected options:");
        int k = 0;
        for (String selected : selectedAnswers) {
            System.out.println(format("  %s: %s", indxes[k++], selected));
        }
        question.answers = selectedAnswers;
        questionsPool.put(questionText, question);
        System.out.println();
    }

    private void review(BufferedReader in) throws IOException {
        System.out.println("List incorrect questions one per line followed by empty line:");
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if ("".equalsIgnoreCase(line)) {
                return;
            }
            Question q = questionsPool.get(line);
            if (q != null) {
                System.out.println(format("Wrong answer(s) for question '%s' were:", q.text));
                for (String answer : q.answers) {
                    System.out.println("    " + answer);
                }
                q.wrong();
            } else {
                System.out.println(format("Question '%s' not found", line));
            }
        }
    }

    private void loadKnowledgeBase() {
        try {
            if (Files.exists(QUESTIONS_POOL_FILE)) {
                String questionsPoolJson = Files.readString(QUESTIONS_POOL_FILE);
                Gson gson = new Gson();
                questionsPool.putAll(gson.fromJson(questionsPoolJson, new TypeToken<Map<String, Question>>() {
                }.getType()));
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void saveKnowledgeBase() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(questionsPool);
        try {
            Files.writeString(QUESTIONS_POOL_FILE, json);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
