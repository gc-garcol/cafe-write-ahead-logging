package gc.garcol.walcore;

import java.nio.ByteBuffer;

/**
 * The `LogReader` interface defines a functional interface for handling log entry data.
 *
 * <p> Implementations of this interface provide a method to process log entry data stored in a `ByteBuffer`.
 * This interface is used in conjunction with the `LogRepository` class to read and process log entries.
 *
 * @author thaivc
 * @since 2024
 */
@FunctionalInterface
public interface LogReader
{
    /**
     * Handles log entry data.
     *
     * <p> This method is called to process log entry data stored in the provided `ByteBuffer`.
     * Implementations should define the logic to handle the log entry data.
     *
     * @param readBuffer the buffer containing the log entry data
     */
    void handle(ByteBuffer readBuffer);
}
