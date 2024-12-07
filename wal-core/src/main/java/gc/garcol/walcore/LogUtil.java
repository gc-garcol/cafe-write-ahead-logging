package gc.garcol.walcore;

import java.io.File;

/**
 * The `LogUtil` class provides utility methods for log file operations.
 * It includes methods to generate log and index file names based on a segment number,
 * and to create directories if they do not exist.
 *
 * <p> This class is thread-safe and ensures that the log directory is created if it does not exist.
 *
 * @author thaivc
 * @since 2024
 */
public class LogUtil
{

    private static final int LONG_LENGTH = String.valueOf(Long.MAX_VALUE).length();
    private static final String LOG_FORMAT = "%0" + LONG_LENGTH + "d" + ".dat";
    private static final String INDEX_FORMAT = "%0" + LONG_LENGTH + "d" + ".index.dat";

    /**
     * Generates the log file name for the specified segment.
     *
     * @param segment the segment number
     * @return the log file name
     */
    public static String logName(long segment)
    {
        return String.format(LOG_FORMAT, segment);
    }

    /**
     * Generates the index file name for the specified segment.
     *
     * @param segment the segment number
     * @return the index file name
     */
    public static String indexName(long segment)
    {
        return String.format(INDEX_FORMAT, segment);
    }

    /**
     * Extracts the segment number from the specified file name.
     *
     * @param fileName the file name
     * @return the segment number
     */
    public static long segment(String fileName)
    {
        return Long.parseLong(fileName.substring(0, fileName.indexOf('.')));
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param pathStr the path of the directory
     */
    public static void createDirectoryNX(String pathStr)
    {
        File dir = new File(pathStr);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }

}
