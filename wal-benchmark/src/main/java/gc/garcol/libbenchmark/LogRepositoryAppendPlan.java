package gc.garcol.libbenchmark;

import gc.garcol.walcore.LogRepository;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author thaivc
 * @since 2024
 */
@State(Scope.Benchmark)
public class LogRepositoryAppendPlan
{
    LogRepository logRepository;
    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    @Setup(Level.Trial)
    public void setUp(Blackhole blackhole) throws IOException
    {
        logRepository = new LogRepository("benchmark/logs");
        logRepository.switchToSegment(
            logRepository.getLatestSegment() < 0
                ? LogRepository.FIRST_SEGMENT
                : logRepository.getLatestSegment()
        );
        writeBuffer.put("Hello, World!".getBytes());
        writeBuffer.flip();
    }
}
