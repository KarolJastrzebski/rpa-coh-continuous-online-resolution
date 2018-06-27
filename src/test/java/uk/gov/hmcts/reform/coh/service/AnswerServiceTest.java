package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    private AnswerService answerService;

    private Answer answer;

    private static final Long ONE = 1L;

    private AnswerState issuedState;
    private AnswerState draftedState;
    private AnswerState submittedState;

    private Answer source;

    @Before
    public void setup() {
        answer = new Answer();
        answerService = new AnswerService(answerRepository);

        draftedState = new AnswerState();
        draftedState.setState("DRAFTED");
        draftedState.setAnswerStateId(1);

        issuedState = new AnswerState();
        issuedState.setState("ISSUED");
        issuedState.setAnswerStateId(3);

        submittedState = new AnswerState();
        submittedState.setState("SUBMITTED");
        submittedState.setAnswerStateId(2);

        source = new Answer();
        source.setAnswerId(ONE);
        source.setAnswerState(draftedState);
        source.setAnswerStateHistories(new ArrayList<>());
        source.setQuestion(new Question());
        source.setAnswerText("foo");
    }

    @Test
    public void testCreateAnswer() {
        when(answerRepository.save(answer)).thenReturn(answer);

        Answer newAnswer = answerService.createAnswer(answer);
        assertEquals(newAnswer, answer);
    }

    @Test
    public void testRetrieveAnswerById() {
        when(answerRepository.findById(ONE)).thenReturn(Optional.of(answer));
        Optional<Answer> newAnswer = answerService.retrieveAnswerById(ONE);
        assertTrue(newAnswer.isPresent());
    }

    @Test
    public void testRetrieveAnswerByIdFail() {
        when(answerRepository.findById(ONE)).thenReturn(Optional.empty());
        Optional<Answer> newAnswer = answerService.retrieveAnswerById(ONE);
        assertFalse(newAnswer.isPresent());
    }

    @Test
    public void testFindByQuestion() {
        Question question = new Question();
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);
        when(answerRepository.findByQuestion(question)).thenReturn(answerList);
        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
        assertFalse("Not empty", answers.isEmpty());
        assertEquals("Answers count", 1, answers.size());
    }

    @Test
    public void testUpdateAnswerById() {
        when(answerRepository.existsById(ONE)).thenReturn(true);
        when(answerRepository.save(answer)).thenReturn(answer);
        answer.setAnswerId(ONE);
        Answer newAnswer = answerService.updateAnswerById(answer);
        assertEquals(newAnswer, answer);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAnswerByIdFail() {
        when(answerRepository.existsById(ONE)).thenReturn(true);
        when(answerRepository.save(answer)).thenReturn(answer);
        answerService.updateAnswerById(answer);
    }

    @Test
    public void testDelete() {
        doNothing().when(answerRepository).delete(answer);
        answerService.deleteAnswer(answer);
        verify(answerRepository, times(1)).delete(answer);
    }

    @Test
    public void testEditAnswerWithStateUpdate(){
        Answer body = new Answer();
        body.setAnswerId(ONE);
        body.setAnswerState(issuedState);
        body.setAnswerText("foo");
        when(answerRepository.findById(ONE)).thenReturn(Optional.of(source));

        answerService.editAnswer(body);
        assertEquals(issuedState, source.getAnswerState());
        assertTrue(source.getAnswerStateHistories().size()==1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testEditAnswerWithIncorrectAnswerIdThrowsEntityNotFoundException(){
        Answer body = new Answer();
        body.setAnswerId(2L);
        body.setAnswerState(issuedState);
        body.setAnswerText("foo");
        when(answerRepository.findById(ONE)).thenReturn(Optional.of(source));

        answerService.editAnswer(body);
    }

    @Test
    public void testUpdateAnswerRecordChangesMade(){
        Answer target = new Answer();
        target.setAnswerId(ONE);
        target.setAnswerState(issuedState);
        target.setAnswerText("foo");

        source = answerService.updateAnswer(source, target);
        assertEquals(issuedState, source.getAnswerState());
        assertTrue(source.getAnswerStateHistories().size()==1);
    }

    @Test
    public void testUpdateAnswerRecordHoldsMultipleStateChanges(){
        Answer target = new Answer();
        target.setAnswerId(ONE);
        target.setAnswerState(submittedState);
        target.setAnswerText("foo");

        source = answerService.updateAnswer(source, target);
        assertEquals(submittedState, source.getAnswerState());
        assertTrue(source.getAnswerStateHistories().size()==1);

        target.setAnswerState(issuedState);
        source = answerService.updateAnswer(source, target);
        assertEquals(issuedState, source.getAnswerState());
        assertTrue(source.getAnswerStateHistories().size()==2);
    }

    @Test(expected = InputMismatchException.class)
    public void testUpdateAnswerDoesNotUpdateIfIdsDoNotMatch(){
        Answer target = new Answer();
        target.setAnswerId(2L);
        target.setAnswerState(issuedState);
        target.setAnswerText("foo");

        source = answerService.updateAnswer(source, target);
        assertEquals(draftedState, source.getAnswerState());
        assertTrue(source.getAnswerStateHistories().size()==0);
    }
}
