package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;

@Repository
public interface EventForwardingRegisterRepository extends CrudRepository<EventForwardingRegister, Integer> {
}
