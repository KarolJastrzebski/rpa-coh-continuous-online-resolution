package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings")
public class OnlineHearingController {

    /**
     * TODO - Don't hard code the starting state
     */
    private static final String STARTING_STATE = "continuous_online_hearing_started";

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    @ApiOperation(value = "Get Online Hearing", notes = "A GET request with a request body is used to retrieve an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{onlineHearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearingResponse> retrieveOnlineHearing(@PathVariable String onlineHearingId ) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(onlineHearingId));
        Optional<OnlineHearing> retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (!retrievedOnlineHearing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OnlineHearingResponse response = new OnlineHearingResponse();
        OnlineHearingMapper.map(response, retrievedOnlineHearing.get());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Filter for Online Hearings", notes = "A GET request with query string containing one or more instances of case_id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearingsResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OnlineHearingsResponse retrieveOnlineHearings(@RequestParam("case_id") List<String> caseIds) {

        List<OnlineHearing> onlineHearings = onlineHearingService.retrieveOnlineHearingByCaseId(caseIds);

        List<OnlineHearingResponse> responses = new ArrayList<>();
        OnlineHearingsResponse onlineHearingsResponse = new OnlineHearingsResponse();
        onlineHearingsResponse.setOnlineHearingResponses(responses);
        for (OnlineHearing onlineHearing : onlineHearings) {
            OnlineHearingResponse response = new OnlineHearingResponse();
            OnlineHearingMapper.map(response, onlineHearing);
            responses.add(response);
        }

        return onlineHearingsResponse;
    }

    @ApiOperation(value = "Create Online Hearing", notes = "A POST request is used to create an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = CreateOnlineHearingResponse.class),
            @ApiResponse(code = 201, message = "Created", response = CreateOnlineHearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateOnlineHearingResponse> createOnlineHearing(@RequestBody OnlineHearingRequest body) {

        OnlineHearing onlineHearing = new OnlineHearing();

        if (StringUtils.isEmpty(body.getCaseId()) || body.getPanel() == null || body.getPanel().isEmpty()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        for (OnlineHearingRequest.PanelMember member : body.getPanel()) {
            if (StringUtils.isEmpty(member.getIdentityToken()) || StringUtils.isEmpty(member.getName())) {
                return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        Optional<OnlineHearingState> onlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(STARTING_STATE);
        if (!onlineHearingState.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        onlineHearing.setOnlineHearingState(onlineHearingState.get());
        onlineHearing.setCaseId(body.getCaseId());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setStartDate(body.getStartDate());

        OnlineHearing createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);
        CreateOnlineHearingResponse response = new CreateOnlineHearingResponse();

        for (OnlineHearingRequest.PanelMember member : body.getPanel()) {
            OnlineHearingPanelMember ohpMember = new OnlineHearingPanelMember();
            ohpMember.setFullName(member.getName());
            ohpMember.setIdentityToken(member.getIdentityToken());
            ohpMember.setOnlineHearing(onlineHearing);
            onlineHearingPanelMemberService.createOnlineHearing(ohpMember);
        }

        response.setOnlineHearingId(createdOnlineHearing.getOnlineHearingId().toString());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
