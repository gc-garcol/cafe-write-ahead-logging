package gc.garcol.libbenchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author thaivc
 * @since 2024
 */
public class LogRepositoryLargeDataAppendRunner {

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
            .include(LogRepositoryLargeDataAppendBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("benchmark-result.LogRepositoryLargeDataAppend.json")
            .jvmArgs("--add-opens", "java.base/java.nio=ALL-UNNAMED") // Add JVM argument
            .build();
        new Runner(options).run();
    }

}
