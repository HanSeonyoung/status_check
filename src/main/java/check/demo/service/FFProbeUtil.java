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
        List<String> command = new ArrayList<>(List.of(
                "ffprobe",
                "-v", "error",
                "-rtsp_transport", "tcp",
                "-rw_timeout", "5000000",               // 안전한 대체 옵션
                "-analyzeduration", "1000000",     // 분석 시간 최소화
                "-probesize", "32",                // 스트림 초기 샘플 크기 최소화
                "-show_entries", "stream=codec_name:format=duration",
                "-of", "json=c=1",
                rtspUrl
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false); // stderr 따로

        try {
            long startTime = System.nanoTime();
            Process process = builder.start();

            // 캡처: stdout
            String stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            // 캡처: stderr
            String stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n")).toLowerCase();

            long endTime = System.nanoTime();  // 종료 시간 측정
            long durationMs = (endTime - startTime) / 1_000_000;

            log.info("FFprobe RTT (ms): {}", durationMs);

            boolean finished = process.waitFor(6, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("FFprobe process timeout for URL: {}", rtspUrl);
                return new FFProbeResult(FFProbeResult.Status.TIMEOUT, stdout, "Process timeout");
            }

            int exitCode = process.exitValue();

            if (stderr.contains("connection refused") || stderr.contains("connection timed out") || stderr.contains("no route to host")) {
                log.warn("Port unreachable: {}", stderr);
                return new FFProbeResult(FFProbeResult.Status.PORT_UNREACHABLE, stdout, stderr);
            }

            if (exitCode != 0 || stderr.contains("error") || stderr.contains("invalid") || stderr.contains("fail")) {
                log.error("FFprobe error (exitCode={}): stderr={}", exitCode, stderr);
                return new FFProbeResult(FFProbeResult.Status.ERROR, stdout, stderr);
            }

            log.debug("FFprobe success: stdout={}", stdout);
            return new FFProbeResult(FFProbeResult.Status.OK, stdout, stderr);

        } catch (IOException | InterruptedException e) {
            log.error("Exception while running FFprobe: {}", e.getMessage(), e);
            return new FFProbeResult(FFProbeResult.Status.ERROR, "", e.getMessage());
        }
    }
}