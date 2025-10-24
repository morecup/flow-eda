package com.flow.eda.runner.flow;

import com.flow.eda.runner.http.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowRequest extends PageRequest {
    private String name;
    private String username;
    private Flow.Status status;
}
