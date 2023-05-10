package org.wangyang.paxis.applicationservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.wangyang.paxis.api.request.AcceptRequest;
import org.wangyang.paxis.api.request.LearnRequest;
import org.wangyang.paxis.api.request.PrepareRequest;
import org.wangyang.paxis.api.response.PrepareResponse;
import org.wangyang.paxis.entity.AcceptedProposalValue;
import org.wangyang.paxis.entity.AcceptedProposalValueId;
import org.wangyang.paxis.entity.LearnedValue;
import org.wangyang.paxis.entity.MinAcceptableProposal;
import org.wangyang.paxis.entity.NextProposalNumber;
import org.wangyang.paxis.repo.AcceptedProposalValueRepository;
import org.wangyang.paxis.repo.LearnedValueRepository;
import org.wangyang.paxis.repo.MinAcceptableProposalRepository;
import org.wangyang.paxis.repo.NextProposalNumberRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaxisApplicationService {
    private final MinAcceptableProposalRepository minAcceptableProposalRepository;
    private final AcceptedProposalValueRepository acceptedProposalValueRepository;
    private final NextProposalNumberRepository nextProposalNumberRepository;
    private final LearnedValueRepository learnedValueRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${paxos.cluster-size}")
    private Integer clusterSize;

    @Value("#{'${spring.application.name}'.split('-')[1]}")
    private Integer idx;

    @Value("#{'${paxos.node-list}'.split(',')}")
    private List<String> nodeList;

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
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
    public void propose(Long instanceNumber, String proposedValue) {
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

        List<PrepareResponse> prepareResponseList = nodeList.stream().map(node -> {
            if (node.equals(applicationName)) {
                return prepare(instanceNumber, nextProposalNumber);
            }
            String url = String.format("http://%s:8080/prepare", constructHostName(node));
            PrepareRequest prepareRequest = new PrepareRequest(instanceNumber, nextProposalNumber);
            try {
                return restTemplate.postForEntity(url, prepareRequest, PrepareResponse.class).getBody();
            } catch (RestClientException e) {
                log.error("failed to get prepare response", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (prepareResponseList.size() <= clusterSize / 2) {
            throw new IllegalStateException(String.format("prepare stage get only %s responses", prepareResponseList.size()));
        }

        String selectedValue = prepareResponseList.stream().filter(response -> Objects.nonNull(response.getProposalNumber()))
                .max(Comparator.comparing(PrepareResponse::getProposalNumber)).map(PrepareResponse::getProposalValue)
                .orElse(proposedValue);

        int acceptedCount = nodeList.stream().mapToInt(node -> {
            if (node.equals(applicationName)) {
                accept(instanceNumber, nextProposalNumber, selectedValue);
                return 1;
            }
            String url = String.format("http://%s:8080/accept", constructHostName(node));
            AcceptRequest acceptRequest = new AcceptRequest(instanceNumber, nextProposalNumber, selectedValue);
            try {
                restTemplate.postForEntity(url, acceptRequest, Void.class);
                return 1;
            } catch (RestClientException e) {
                log.error("failed to get accept response", e);
                return 0;
            }
        }).sum();

        if (acceptedCount <= clusterSize / 2) {
            throw new IllegalArgumentException(String.format(String.format("accept stage failed with %s responses", acceptedCount)));
        }

        // learn value and propagate
        nodeList.forEach(node -> {
            if (node.equals(applicationName)) {
                learn(instanceNumber, selectedValue);
                return;
            }
            String url = String.format("http://%s:8080/learn", constructHostName(node));
            LearnRequest learnRequest = new LearnRequest(instanceNumber, selectedValue);
            try {
                restTemplate.postForEntity(url, learnRequest, Void.class);
            } catch (RestClientException e) {
                log.error("failed to propagate proposal value", e);
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void learn(Long instanceNumber, String proposalValue) {
        Optional<LearnedValue> learnedValueOptional = learnedValueRepository.findById(instanceNumber);
        if (learnedValueOptional.isPresent()) {
            return;
        }

        learnedValueRepository.save(LearnedValue.builder().instanceNumber(instanceNumber).proposalValue(proposalValue)
                .build());
    }

    private String constructHostName(String podName) {
        return podName + ".paxis";
    }

    public String env() {
        return String.join("\n", applicationName, clusterSize.toString(), idx.toString(), nodeList.toString());
    }

    public String learnedValue(Long instanceNumber) {
        return learnedValueRepository.findById(instanceNumber).map(LearnedValue::getProposalValue).orElse(null);
    }
}
