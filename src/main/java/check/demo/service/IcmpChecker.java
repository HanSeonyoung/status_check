package check.demo.service;

import check.demo.model.IcmpResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class IcmpChecker {

    public IcmpResult check(String ip) {
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

            while ((line = reader.readLine()) != null) {
                if (line.contains("packets transmitted") || line.contains("패킷을 보냈고")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        packetLoss = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
                    }
                } else if (line.contains("rtt min/avg/max") || line.contains("평균 =")) {
                    String[] parts = line.split("=");
                    if (parts.length >= 2) {
                        String[] rttParts = parts[1].trim().split("/");
                        avgRtt = Double.parseDouble(rttParts[1]);
                    }
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
}
