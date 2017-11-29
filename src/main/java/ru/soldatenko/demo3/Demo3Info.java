package ru.soldatenko.demo3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Demo3Info {
    private String requestUri;
    private String callerDn;
    private String sessionId;
    private LinkedHashMap<String, Boolean> groupMembership;
}
