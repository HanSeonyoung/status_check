package check.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IcmpResult {
    public enum Status {
        OK, TIMEOUT, FAILED, UNDEFINED
    }

    private final Status status;
    private final boolean success;
    private final Double avgRttMs;
    private final Double maxRttMs;
    private final Double minRttMs;
    private final Double packetLossPct;

}
