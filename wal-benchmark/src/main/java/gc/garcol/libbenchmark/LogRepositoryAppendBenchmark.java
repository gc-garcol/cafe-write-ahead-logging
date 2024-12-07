package gc.garcol.libbenchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author thaivc
 * @since 2024
 */
@State(Scope.Thread)
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class LogRepositoryAppendBenchmark
{
    @Benchmark
    @Timeout(time = 60)
    @Measurement(iterations = 1, time = 60)
    @Warmup(iterations = 1, time = 10)
    public void publish(LogRepositoryAppendPlan plan, Blackhole blackhole) throws IOException
    {
        plan.logRepository.append(plan.writeBuffer);
        plan.writeBuffer.flip(); // Reset buffer for next write
    }

}
