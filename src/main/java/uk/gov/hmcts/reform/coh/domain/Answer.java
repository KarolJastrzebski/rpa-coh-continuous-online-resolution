package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "answer")
public class Answer {

    @SequenceGenerator(name="seq_answer_id", sequenceName="seq_answer_id", allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_answer_id")
    @Id
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "answer_text")
    private String answerText;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Answer answerId(Long answerId) {
        this.answerId = answerId;
        return this;
    }

    public Answer answerText(String answerText) {
        this.answerText = answerText;
        return this;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
