// src/main/java/check/demo/service/IcmpChecker.java
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
            Pattern linuxRtt  = Pattern.compile("(?:rtt|round-trip) min/avg/max/(?:mdev|stddev) = ([\\d.]+)/([\\d.]+)/([\\d.]+)/");
            Pattern linuxLoss = Pattern.compile("(\\d+)%\\s*packet loss");
            // Windows(영/한) 예: "Minimum = 10ms, Maximum = 30ms, Average = 20ms" / "최소 = 10ms, 최대 = 30ms, 평균 = 20ms"
            Pattern winRtt    = Pattern.compile("(?:Minimum|최소)\\s*=\\s*(\\d+)ms,\\s*(?:Maximum|최대)\\s*=\\s*(\\d+)ms,\\s*(?:Average|평균)\\s*=\\s*(\\d+)ms");
            Pattern winLoss   = Pattern.compile("\\((\\d+)%\\s*(?:loss|손실)\\)");

            while ((line = reader.readLine()) != null) {
                // 손실률
                Matcher m1 = linuxLoss.matcher(line);
                Matcher m2 = winLoss.matcher(line);
                if (m1.find()) packetLoss = Double.valueOf(m1.group(1));
                else if (m2.find()) packetLoss = Double.valueOf(m2.group(1));

                // RTT
                Matcher r1 = linuxRtt.matcher(line);
                Matcher r2 = winRtt.matcher(line);
                if (r1.find()) {
                    rttMin = Double.valueOf(r1.group(1));
                    rttAvg = Double.valueOf(r1.group(2));
                    rttMax = Double.valueOf(r1.group(3));
                } else if (r2.find()) {
                    rttMin = Double.valueOf(r2.group(1));
                    rttMax = Double.valueOf(r2.group(2));
                    rttAvg = Double.valueOf(r2.group(3));
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return new IcmpResult(IcmpResult.Status.TIMEOUT, false, rttAvg, packetLoss, rttMin, rttMax);
            }
            return new IcmpResult(IcmpResult.Status.OK, true, rttAvg, packetLoss, rttMin, rttMax);

        } catch (Exception e) {
            return new IcmpResult(IcmpResult.Status.FAILED, false, null, null, null, null);
        }
    }

    private boolean isValidIp(String ip) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
        return ip != null && ip.matches(ipPattern);
    }
}
