package check.demo.service;

import check.demo.model.IcmpResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IcmpChecker {

    public IcmpResult check(String ip) {
        // 입력값 검증
        if (ip == null || ip.trim().isEmpty() || !isValidIp(ip)) {
            return new IcmpResult(IcmpResult.Status.UNDEFINED, false, null, null);
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;

            if (os.contains("win")) {
                builder = new ProcessBuilder("ping", "-n", "4", "-w", "1000", ip);
            } else {
                builder = new ProcessBuilder("ping", "-c", "4", "-W", "1", ip);
            }

            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            double avgRtt = -1;
            double packetLoss = -1;

            Pattern packetPattern = Pattern.compile("(\\d+)%?\\s*packet loss|패킷.*(\\d+)%");
            Pattern rttPattern = Pattern.compile("rtt min/avg/max.*=.*\\d+\\.\\d+/(\\d+\\.\\d+)|평균 = (\\d+)ms");

            while ((line = reader.readLine()) != null) {
                // 패킷 손실률 파싱
                Matcher packetMatcher = packetPattern.matcher(line);
                if (packetMatcher.find()) {
                    String lossStr = packetMatcher.group(1);
                    packetLoss = lossStr != null ? Double.parseDouble(lossStr) : null;
                }

                // RTT 파싱
                Matcher rttMatcher = rttPattern.matcher(line);
                if (rttMatcher.find()) {
                    String rttStr = rttMatcher.group(1) != null ? rttMatcher.group(1) : rttMatcher.group(2);
                    avgRtt = rttStr != null ? Double.parseDouble(rttStr) : null;
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return new IcmpResult(IcmpResult.Status.TIMEOUT, false, avgRtt, packetLoss);
            }

            return new IcmpResult(IcmpResult.Status.OK, true, avgRtt, packetLoss);

        } catch (Exception e) {
            return new IcmpResult(IcmpResult.Status.FAILED, false, null, null);
        }
    }

    private boolean isValidIp(String ip) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
        return ip != null && ip.matches(ipPattern);
    }
}
