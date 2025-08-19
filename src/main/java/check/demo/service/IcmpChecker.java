// src/main/java/check/demo/service/IcmpChecker.java
package check.demo.service;

import check.demo.model.IcmpResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IcmpChecker {

    public IcmpResult check(String ip) {
        if (ip == null || ip.trim().isEmpty() || !isValidIp(ip)) {
            return new IcmpResult(IcmpResult.Status.UNDEFINED, false, null, null, null, null);
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder = os.contains("win")
                    ? new ProcessBuilder("ping", "-n", "4", "-w", "1000", ip)
                    : new ProcessBuilder("ping", "-c", "4", "-W", "1", ip);

            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // ⬇️ 없으면 null 유지
            Double rttMin = null, rttAvg = null, rttMax = null;
            Double packetLoss = null;

            // Linux 예: "rtt min/avg/max/mdev = 0.041/0.052/0.067/0.010 ms"
            // Windows(영/한) 예: "Minimum = 10ms, Maximum = 30ms, Average = 20ms" / "최소 = 10ms, 최대 = 30ms, 평균 = 20ms"

            if (os.contains("win")) {
                Pattern winLossPattern = Pattern.compile("\\((\\d+)%\\s*(?:loss|손실)\\)");
                Pattern winRttPattern = Pattern.compile("(?:Minimum|최소)\\s*=\\s*(\\d+)ms,\\s*(?:Maximum|최대)\\s*=\\s*(\\d+)ms,\\s*(?:Average|평균)\\s*=\\s*(\\d+)ms");

                while ((line = reader.readLine()) != null) {
                    Matcher lossMatcher = winLossPattern.matcher(line);
                    if (lossMatcher.find() && packetLoss == null) {
                        packetLoss = Double.valueOf(lossMatcher.group(1));
                    }
                    Matcher rttMatcher = winRttPattern.matcher(line);
                    if (rttMatcher.find()) {
                        rttMin = Double.valueOf(rttMatcher.group(1));
                        rttMax = Double.valueOf(rttMatcher.group(2));
                        rttAvg = Double.valueOf(rttMatcher.group(3));
                    }
                }
            } else {
                Pattern linuxLossPattern = Pattern.compile("(\\d+)%\\s*packet loss");
                Pattern linuxRttPattern = Pattern.compile(
                        "(?:rtt|round-trip) min/avg/max/(?:mdev|stddev) = ([\\d.]+)/([\\d.]+)/([\\d.]+)/[\\d.]+ ms"
                );

                while ((line = reader.readLine()) != null) {
                    Matcher lossMatcher = linuxLossPattern.matcher(line);
                    if (lossMatcher.find() && packetLoss == null) {
                        packetLoss = Double.valueOf(lossMatcher.group(1));
                    }
                    Matcher rttMatcher = linuxRttPattern.matcher(line);
                    if (rttMatcher.find()) {
                        rttMin = Double.valueOf(rttMatcher.group(1));
                        rttAvg = Double.valueOf(rttMatcher.group(2));
                        rttMax = Double.valueOf(rttMatcher.group(3));
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return new IcmpResult(IcmpResult.Status.TIMEOUT, false, rttAvg, rttMin, rttMax, packetLoss);
            }
            return new IcmpResult(IcmpResult.Status.OK, true, rttAvg, rttMin, rttMax, packetLoss);

        } catch (Exception e) {
            return new IcmpResult(IcmpResult.Status.FAILED, false, null, null, null, null);
        }
    }

    private boolean isValidIp(String ip) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
        return ip != null && ip.matches(ipPattern);
    }
}
