package org.wangyang.paxis.applicationservice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wangyang.paxis.api.response.PrepareResponse;
import org.wangyang.paxis.entity.AcceptedProposalValue;
import org.wangyang.paxis.entity.AcceptedProposalValueId;
import org.wangyang.paxis.entity.MinAcceptableProposal;
import org.wangyang.paxis.entity.NextProposalNumber;
import org.wangyang.paxis.repo.AcceptedProposalValueRepository;
import org.wangyang.paxis.repo.MinAcceptableProposalRepository;
import org.wangyang.paxis.repo.NextProposalNumberRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaxisApplicationService {
    private final MinAcceptableProposalRepository minAcceptableProposalRepository;
    private final AcceptedProposalValueRepository acceptedProposalValueRepository;
    private final NextProposalNumberRepository nextProposalNumberRepository;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${paxos.cluster-size}")
    private Integer clusterSize;

    @Value("${paxos.idx}")
    private Integer idx;

    @Value("#{'${paxos.node-list}'.split(',')}")
    private List<String> nodeList;

    @Transactional
    public PrepareResponse prepare(Long instanceNumber, Long proposalNumber) {
        Optional<MinAcceptableProposal> minAcceptableProposalOptional = minAcceptableProposalRepository.findByInstanceNumber(instanceNumber);

        minAcceptableProposalOptional.ifPresentOrElse(proposal -> {
            if (proposalNumber < proposal.getProposalNumber()) {
                throw new IllegalArgumentException(String.format("proposalNumber %s < %s is not acceptable", proposalNumber, proposal.getProposalNumber()));
            }

            proposal.setProposalNumber(proposalNumber + 1);
            minAcceptableProposalRepository.save(proposal);
        }, () -> {
            MinAcceptableProposal minAcceptableProposal = MinAcceptableProposal.create(instanceNumber, proposalNumber + 1);
            minAcceptableProposalRepository.save(minAcceptableProposal);
        });

        Optional<AcceptedProposalValue> highestAcceptedProposalValue = acceptedProposalValueRepository.findTopOneByInstanceNumberAndProposalNumberLessThanOrderByProposalNumberDesc(instanceNumber, proposalNumber);
        if (highestAcceptedProposalValue.isEmpty()) {
            return PrepareResponse.builder()
                    .applicationName(applicationName)
                    .instanceNumber(instanceNumber)
                    .build();
        }
        return PrepareResponse.builder()
                .applicationName(applicationName)
                .instanceNumber(instanceNumber)
                .proposalNumber(highestAcceptedProposalValue.get().getProposalNumber())
                .proposalValue(highestAcceptedProposalValue.get().getProposalValue())
                .build();
    }

    @Transactional
    public void accept(Long instanceNumber, Long proposalNumber, String proposalValue) {
        Optional<MinAcceptableProposal> minAcceptableProposalOptional = minAcceptableProposalRepository.findByInstanceNumber(instanceNumber);
        minAcceptableProposalOptional.ifPresent(minAcceptableProposal -> {
            if (proposalNumber < minAcceptableProposal.getProposalNumber() - 1) {
                throw new IllegalArgumentException(String.format("proposalNumber %s < %s is not acceptable", proposalNumber, minAcceptableProposal.getProposalNumber() - 1));
            }
        });

        acceptedProposalValueRepository.findById(AcceptedProposalValueId.builder()
                .instanceNumber(instanceNumber)
                .proposalNumber(proposalNumber).build()).ifPresent(acceptedProposalValue -> {
                    throw new IllegalArgumentException(String.format("%s - %s with value %s already accepted", instanceNumber, proposalNumber, acceptedProposalValue.getProposalValue()));
        });

        acceptedProposalValueRepository.save(AcceptedProposalValue.builder()
                .instanceNumber(instanceNumber)
                .proposalNumber(proposalNumber)
                .proposalValue(proposalValue)
                .build());
    }

    @Transactional(rollbackFor = Exception.class)
    public void propose(Long instanceNumber, String proposalValue) {
        Optional<NextProposalNumber> nextProposalNumberOptional = nextProposalNumberRepository.findById(instanceNumber);
        Long nextProposalNumber;
        if (nextProposalNumberOptional.isEmpty()) {
            nextProposalNumber = idx.longValue();
            nextProposalNumberRepository.save(NextProposalNumber.builder()
                    .instanceNumber(instanceNumber)
                    .proposalNumber(nextProposalNumber + clusterSize).build());
        } else {
            nextProposalNumber = nextProposalNumberOptional.get().getProposalNumber();
            nextProposalNumberOptional.get().setProposalNumber(nextProposalNumber + clusterSize);
            nextProposalNumberRepository.save(nextProposalNumberOptional.get());
        }
    }
}
