package nl.martenm.migrationm.api;

import java.io.InputStream;

/**
 * Provides a way to access an input stream without requiring the InputStream object to be created.
 * Only when the InputStream is required will this method be called.
 */
public interface InputStreamProvider {

    /**
     * Loads and provides the InputStream.
     * Note that before calling this method the InputStream might not have been created yet.
     * @return The InputStream
     */
    InputStream getInputStream();

}
