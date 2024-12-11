package gc.garcol.libbenchmark;

import gc.garcol.walcore.LogRepository;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

/**
 * @author thaivc
 * @since 2024
 */
@State(Scope.Benchmark)
public class LogRepositoryLargeDataAppendPlan
{
    String data = "Hello, World!";
    int repeat = 5000;
    LogRepository logRepository;
    ByteBuffer writeBuffer = ByteBuffer.allocate(repeat * data.length());

    @Setup(Level.Trial)
    public void setUp(Blackhole blackhole) throws IOException
    {
        logRepository = new LogRepository("benchmark/logs");
        logRepository.switchToSegment(
            logRepository.getLatestSegment() < 0
                ? LogRepository.FIRST_SEGMENT
                : logRepository.getLatestSegment()
        );
        writeBuffer.put(data.repeat(repeat).getBytes());
        writeBuffer.flip();
    }
}
