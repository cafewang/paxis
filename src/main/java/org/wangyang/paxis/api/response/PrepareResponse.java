package org.wangyang.paxis.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrepareResponse {
    private final String applicationName;
    private final Long instanceNumber;
    private final Long proposalNumber;
    private final String proposalValue;
}
