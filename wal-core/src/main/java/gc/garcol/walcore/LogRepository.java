package gc.garcol.walcore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

/**
 * The `LogRepository` class provides methods to read, write, truncate, and get the total records of
 * log files. It uses `RandomAccessFile` and `FileChannel` to perform file operations.
 *
 * @author thaivc
 * @since 2024
 */
public class LogRepository
{

    /**
     * The first segment number.
     */
    public static final long FIRST_SEGMENT = 0;

    private static ByteBuffer indexBufferWriter = ByteBuffer.allocate(LogIndex.SIZE);
    private static ByteBuffer indexBufferReader = ByteBuffer.allocate(LogIndex.SIZE);

    private final String baseLogDir;

    long currentSegment;
    long currentIndex;

    RandomAccessFile indexFile;
    RandomAccessFile logFile;
    FileChannel indexChannel;
    FileChannel logChannel;

    long logOffset;
    long indexOffset;

    /**
     * Constructs a `LogRepository` with the specified base log directory.
     *
     * @param baseLogDir the base directory for log files
     */
    public LogRepository(String baseLogDir)
    {
        LogUtil.createDirectoryNX(baseLogDir);
        this.baseLogDir = baseLogDir;
    }

    /**
     * Retrieves the latest log segment number from the base log directory.
     *
     * <p> This method scans the base log directory for log files, sorts them by file name,
     * and returns the segment number of the most recent log file. If no log files are found, it
     * returns 0.
     *
     * @return the segment number of the latest log file, or 0 if no log files are found
     * @throws IOException if an I/O error occurs while accessing the log directory
     */
    public long getLatestSegment() throws IOException
    {
        Optional<Path> lastFile = Files.list(Paths.get(baseLogDir))
            .filter(Files::isRegularFile)
            .max(Comparator.comparing(Path::getFileName));
        return lastFile.map(path -> LogUtil.segment(path.getFileName().toString())).orElse(-1L);
    }

    private void generateFiles(long segment) throws IOException
    {
        indexFile = new RandomAccessFile(indexPath(segment), "rw");
        logFile = new RandomAccessFile(logPath(segment), "rw");
        currentIndex = totalRecords(currentSegment);
        indexChannel = indexFile.getChannel();
        logChannel = logFile.getChannel();
        logOffset = logFile.length();
        indexOffset = indexFile.length();
    }

    /**
     * Switches to the specified log segment and initializes the corresponding log and index files.
     *
     * <p> This method updates the current segment to the specified segment, generates the necessary
     * log and index files,
     * and sets the current index to the total number of records in the new segment.
     *
     * @param segment the segment number to switch to
     * @throws IOException              if an I/O error occurs during the file operations
     * @throws IllegalArgumentException if the specified segment is older than the current segment
     */
    public void switchToSegment(long segment) throws IOException
    {
        long latestSegment = getLatestSegment();
        if (segment < latestSegment)
        {
            throw new IllegalArgumentException("Cannot switch to an older segment");
        }
        currentSegment = segment;
        generateFiles(currentSegment);
        currentIndex = totalRecords(currentSegment);
    }

    /**
     * Reads a log entry from the specified segment and index into the provided `ByteBuffer`.
     *
     * @param segment      the segment of the log files
     * @param index        the index of the log entry to read
     * @param readerBuffer the buffer to read the log entry into
     * @throws IOException if an I/O error occurs
     */
    public void read(long segment, long index, ByteBuffer readerBuffer) throws IOException
    {
        RandomAccessFile indexFileRead = indexFile != null && segment == currentSegment ? indexFile
            : new RandomAccessFile(indexPath(segment), "r");
        RandomAccessFile logFileRead = logFile != null && segment == currentSegment ? logFile
            : new RandomAccessFile(logPath(segment), "r");

        indexFileRead.seek(index * LogIndex.SIZE);
        long logIndex = indexFileRead.readLong();
        int logLength = indexFileRead.readInt();
        readerBuffer.clear();
        readerBuffer.limit(logLength);
        var logChannel = logFileRead.getChannel();
        logChannel.position(logIndex);
        logChannel.read(readerBuffer);
        readerBuffer.flip();
    }

    /**
     * Reads log entries from the specified segment, reading from fromIndex to toIndex, the log entries are then handled by a `LogReader`.
     *
     * @param segment    the segment of the log files
     * @param fromIndex  the starting index of the log entries to read
     * @param toIndex    the ending index of the log entries to read
     * @param readBuffer the buffer to read the log entries into
     * @param logReader  the log reader to handle the log entries
     * @throws IOException if an I/O error occurs
     */
    public void read(long segment, long fromIndex, long toIndex, ByteBuffer readBuffer, LogReader logReader) throws IOException
    {
        RandomAccessFile indexFileRead = indexFile != null && segment == currentSegment ? indexFile
            : new RandomAccessFile(indexPath(segment), "r");
        RandomAccessFile logFileRead = logFile != null && segment == currentSegment ? logFile
            : new RandomAccessFile(logPath(segment), "r");

        readLogInRange(fromIndex, toIndex, readBuffer, logReader, indexFileRead, logFileRead);
    }

    /**
     * Reads log entries from the specified segment and index range, reading from fromIndex to the end of log segment, the log entries are then handled by a `LogReader`.
     *
     * @param segment    the segment of the log files
     * @param fromIndex  the starting index of the log entries to read
     * @param readBuffer the buffer to read the log entries into
     * @param logReader  the log reader to handle the log entries
     * @throws IOException if an I/O error occurs
     */
    public void read(long segment, long fromIndex, ByteBuffer readBuffer, LogReader logReader) throws IOException
    {
        RandomAccessFile indexFileRead = indexFile != null && segment == currentSegment ? indexFile
            : new RandomAccessFile(indexPath(segment), "r");
        RandomAccessFile logFileRead = logFile != null && segment == currentSegment ? logFile
            : new RandomAccessFile(logPath(segment), "r");

        long endIndex = totalRecords(indexFileRead) - 1;

        readLogInRange(fromIndex, endIndex, readBuffer, logReader, indexFileRead, logFileRead);
    }

    private void readLogInRange(final long fromIndex, final long toIndex, final ByteBuffer readBuffer, final LogReader logReader, final RandomAccessFile indexFileRead, final RandomAccessFile logFileRead) throws IOException
    {
        FileChannel logChannel = logFileRead.getChannel();
        for (long index = fromIndex; index <= toIndex; index++)
        {
            indexFileRead.seek(index * LogIndex.SIZE);
            long logIndex = indexFileRead.readLong();
            int logLength = indexFileRead.readInt();
            logFileRead.seek(logIndex);
            readBuffer.clear();
            readBuffer.limit(logLength);
            logChannel.position(logIndex);
            logChannel.read(readBuffer);
            readBuffer.flip();
            logReader.handle(readBuffer);
        }
    }

    /**
     * Appends log entries to the current log segment.
     *
     * <p> This method writes the provided log entries to the current log file and updates the index
     * file accordingly.
     * The log entries are written at the end of the current log file, and the index file is updated
     * with the position and length of each log entry.
     *
     * @param logs the buffer containing the log entries to append
     * @throws IOException if an I/O error occurs during the write operation
     */
    public void append(ByteBuffer logs) throws IOException
    {
        indexBufferWriter.clear();
        indexBufferWriter.putLong(logFile.length());
        indexBufferWriter.putInt(logs.limit());
        indexBufferWriter.flip();

        logChannel.write(logs, logOffset);
        indexChannel.write(indexBufferWriter, indexOffset);
        logOffset += logs.limit();
        indexOffset += LogIndex.SIZE;
        currentIndex++;
    }

    /**
     * Truncates the log files from the specified index.
     *
     * <p> Only use in case follower of Raft
     *
     * @param segment   the segment of the log files
     * @param fromIndex the index from which to truncate
     * @throws IOException if an I/O error occurs
     */
    public void truncate(long segment, long fromIndex) throws IOException
    {
        File indexFileSys = new File(indexPath(segment));
        if (!indexFileSys.exists())
        {
            return;
        }

        try (
            RandomAccessFile indexFile = new RandomAccessFile(indexPath(segment), "rw");
            RandomAccessFile logFile = new RandomAccessFile(logPath(segment), "rw")
        )
        {
            indexBufferReader.clear();
            var indexChannel = indexFile.getChannel();
            indexChannel.read(indexBufferReader, fromIndex * LogIndex.SIZE);
            indexBufferReader.flip();
            LogIndex logIndex = new LogIndex(indexBufferReader.getLong(), indexBufferReader.getInt());
            indexFile.setLength(fromIndex * LogIndex.SIZE);
            logFile.setLength(logIndex.index());
        }
    }

    /**
     * Returns the total number of records in the log files for the specified segment.
     *
     * @param segment the segment of the log files
     * @return the total number of records
     * @throws IOException if an I/O error occurs
     */
    public long totalRecords(long segment) throws IOException
    {
        RandomAccessFile indexFileRead =
            segment == currentSegment ? indexFile : new RandomAccessFile(indexPath(segment), "r");
        return indexFileRead.length() / LogIndex.SIZE;
    }

    /**
     * Returns the total number of records in the specified index file.
     *
     * @param indexFileRead the index file to read
     * @return the total number of records
     * @throws IOException if an I/O error occurs
     */
    public long totalRecords(RandomAccessFile indexFileRead) throws IOException
    {
        return indexFileRead.length() / LogIndex.SIZE;
    }

    private String indexPath(long segment)
    {
        return baseLogDir + "/" + LogUtil.indexName(segment);
    }

    private String logPath(long segment)
    {
        return baseLogDir + "/" + LogUtil.logName(segment);
    }
}
