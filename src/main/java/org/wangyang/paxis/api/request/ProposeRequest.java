package org.wangyang.paxis.api.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@RequiredArgsConstructor
public class ProposeRequest {
    @NotNull
    private final Long instanceNumber;
    @NotNull
    private final String proposalValue;
}
