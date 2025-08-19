package check.demo.service;

import check.demo.model.FFProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FFProbeUtil {

    public static FFProbeResult runFFProbe(String rtspUrl) {
        // 입력값 검증
        if (rtspUrl == null || rtspUrl.trim().isEmpty() || !rtspUrl.startsWith("rtsp://")) {
            log.warn("Invalid RTSP URL: {}", rtspUrl);
            return new FFProbeResult(FFProbeResult.Status.UNDEFINED, "Invalid RTSP URL", "");
        }

        List<String> command = new ArrayList<>(List.of(
                "ffprobe",
                "-v", "error",
                "-rtsp_transport", "tcp",
                "-rw_timeout", "5000000",
                "-analyzeduration", "1000000",
                "-probesize", "32",
                "-show_entries", "stream=codec_name:format=duration",
                "-of", "json=c=1",
                rtspUrl
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false); // stderr 따로

        try {
            long startTime = System.nanoTime();
            Process process = builder.start();

            // stdout
            String stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            // stderr (원문 + lower)
            String stderrRaw = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            String stderrLower = stderrRaw.toLowerCase();

            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("FFprobe elapsed (ms): {}", elapsedMs);

            boolean finished = process.waitFor(6, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("FFprobe process timeout for URL: {}", rtspUrl);
                return new FFProbeResult(FFProbeResult.Status.TIMEOUT, "Process timeout", stdout);
            }

            int exitCode = process.exitValue();

            if (stderrLower.contains("connection refused")
                    || stderrLower.contains("connection timed out")
                    || stderrLower.contains("no route to host")) {
                log.warn("Port unreachable: {}", stderrRaw);
                return new FFProbeResult(FFProbeResult.Status.PORT_UNREACHABLE, stderrRaw, stdout);
            }

            if (exitCode != 0
                    || stderrLower.contains("error")
                    || stderrLower.contains("invalid")
                    || stderrLower.contains("fail")) {
                log.error("FFprobe error (exitCode={}): stderr={}", exitCode, stderrRaw);
                return new FFProbeResult(FFProbeResult.Status.ERROR, stderrRaw, stdout);
            }

            log.debug("FFprobe success: stdout={}", stdout);
            return new FFProbeResult(FFProbeResult.Status.OK, stderrRaw, stdout);

        } catch (IOException | InterruptedException e) {
            log.error("Exception while running FFprobe: {}", e.getMessage(), e);
            return new FFProbeResult(FFProbeResult.Status.ERROR, e.getMessage(), "");
        }
    }
}
