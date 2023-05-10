package org.wangyang.paxis.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepareResponse {
    private String applicationName;
    private Long instanceNumber;
    private Long proposalNumber;
    private String proposalValue;
}
