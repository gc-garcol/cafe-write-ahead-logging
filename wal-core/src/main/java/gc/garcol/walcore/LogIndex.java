package gc.garcol.walcore;

/**
 * The `LogIndex` record represents an index entry in the log file.
 * It contains the position of the log entry and the length of the log entry.
 *
 * <p> This record is immutable and thread-safe.
 *
 * @param index       the position of the log entry
 * @param entryLength the length of the log entry
 * @author thaivc
 * @since 2024
 */
public record LogIndex(
    long index,
    int entryLength
)
{
    /**
     * The size of the `LogIndex` record in bytes.
     */
    public static final int SIZE = Long.BYTES + Integer.BYTES;
}
