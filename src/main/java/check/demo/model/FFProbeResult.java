package check.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FFProbeResult {
    public enum Status {
        OK, TIMEOUT, ERROR, PORT_UNREACHABLE, UNDEFINED
    }

    private final Status status;
    private final String stderr;
    private final String stdout;
}
