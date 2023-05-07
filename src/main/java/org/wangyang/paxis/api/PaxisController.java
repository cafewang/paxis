package org.wangyang.paxis.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wangyang.paxis.api.request.AcceptRequest;
import org.wangyang.paxis.api.request.LearnRequest;
import org.wangyang.paxis.api.request.PrepareRequest;
import org.wangyang.paxis.api.request.ProposeRequest;
import org.wangyang.paxis.api.response.PrepareResponse;
import org.wangyang.paxis.applicationservice.PaxisApplicationService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class PaxisController {
    private final PaxisApplicationService paxisApplicationService;

    @PostMapping("propose")
    public void propose(@RequestBody @Valid ProposeRequest proposeRequest) {
        paxisApplicationService.propose(proposeRequest.getInstanceNumber(), proposeRequest.getProposedValue());
    }

    @PostMapping("prepare")
    public PrepareResponse prepare(@RequestBody @Valid PrepareRequest prepareRequest) {
        return paxisApplicationService.prepare(prepareRequest.getInstanceNumber(), prepareRequest.getProposalNumber());
    }

    @PostMapping("accept")
    public void accept(@RequestBody @Valid AcceptRequest acceptRequest) {
        paxisApplicationService.accept(acceptRequest.getInstanceNumber(), acceptRequest.getProposalNumber(), acceptRequest.getProposalValue());
    }

    @PostMapping("learn")
    public void learn(@RequestBody @Valid LearnRequest learnRequest) {
        paxisApplicationService.learn(learnRequest.getInstanceNumber(), learnRequest.getProposalValue());
    }
}
