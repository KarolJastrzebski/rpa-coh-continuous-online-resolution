package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "question_round")
@SequenceGenerator(name="seq_question_round_id", initialValue=1, allocationSize=100)
public class QuestionRound {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_question_round_id")
    @Column(name = "question_round_id")
    private Long questionRoundId;

    @Column(name = "online_hearing_id")
    private int onlineHearingId;

    @Column(name = "round_number")
    private int roundNumber;

    public Long getQuestionRoundId() {
        return questionRoundId;
    }

    public void setQuestionRoundId(Long questionRoundId) {
        this.questionRoundId = questionRoundId;
    }

    public int getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(int onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
}
