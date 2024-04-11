package org.springframework.samples.petclinic.model;

public class Question {
    private String question;
    private String answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String toString() {
        return "Question: " + this.question + "\n" +
               "Answer: " + this.answer;
    }

    public Question copy() {
        Question question = new Question();
        question.setQuestion(this.question);
        question.setAnswer(this.answer);
        return question;
    }
}
