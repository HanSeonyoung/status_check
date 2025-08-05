package check.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IcmpResult {
    public enum Status {
        OK, TIMEOUT, FAILED
    }

    private final Status status;
    private final boolean success;
    private final Double avgRttMs;
    private final Double packetLossPct;
}
