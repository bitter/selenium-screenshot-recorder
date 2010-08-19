/*
 * Copyright 2010 Martin Sternevald, http://github.com/bitter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bitter.selenium;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.selenium.CommandProcessor;

public class ScreenshotRecorder {

    /** The maximum length of any screenshot filename **/
    public static int DEFAULT_MAX_FILENAME_LENGTH = 250;

    private static final Logger LOG = Logger.getLogger(ScreenshotRecorder.class.getName());
    private static final AtomicInteger ScreenshotCounter = new AtomicInteger();

    // --------------------------------
    // Thread local instance
    // --------------------------------
    private static final ThreadLocal<ScreenshotRecorder> CURRENT = new ThreadLocal<ScreenshotRecorder>();

    /**
     * Needs to be set by a test when using the {@link RecordingCommandProcessor}.
     * @param current
     */
    public static void set(ScreenshotRecorder current) {
        CURRENT.set(current);
    }

    /**
     * Used by {@link RecordingCommandProcessor} to determine which {@link ScreenshotRecorder} to use.
     * @return The currently associated {@link ScreenshotRecorder}
     */
    public static ScreenshotRecorder current() {
        return CURRENT.get();
    }

    // --------------------------------
    // Recorder instance
    // --------------------------------
    private File _screenshotDirectory;
    private final int _maxFileNameLength;

    public ScreenshotRecorder(String directory, String testName) {
        this(directory, testName, DEFAULT_MAX_FILENAME_LENGTH);
    }

    public ScreenshotRecorder(String directory, String testName,
            int maxFileNameLength) {
        _screenshotDirectory = new File(directory, testName).getAbsoluteFile();
        _maxFileNameLength = maxFileNameLength;
    }

    /**
     * Grabs a browser screenshot using the supplied cammand processor.
     *
     * @param cp
     *            a {@link CommandProcessor}
     * @param name
     *            This will be part of the file name (after the initial counter)
     *            though it might be trimmed to fit the maxFileNameLegth.
     * @return The file name of the generated screenshot.
     */
    public File recordScreenshot(CommandProcessor cp, String name) {
        File file = generateScreenshotFileName(name);
        try {
            cp.doCommand("captureEntirePageScreenshot", new String[] {
                    file.getAbsolutePath(), "" });
        } catch (RuntimeException t) {
            LOG.log(Level.WARNING, "Unable to take screen shot:"
                    + file.getAbsolutePath(), t);
            throw t;
        }
        return file;
    }

    /**
     * Package recorded tests in a zip file named equally to the screenshot
     * directory but with a ".zip" suffix.
     *
     * @return The name of the zip file or null if there were no screenshots
     *         available.
     * @throws IOException
     */
    public File packageRecordedScreenshots() throws IOException {
        if (_screenshotDirectory.exists()) {
            File zipFile = new File(_screenshotDirectory + ".zip");
            new de.schlichtherle.io.File(_screenshotDirectory)
                    .copyAllTo(zipFile);
        }
        return null;
    }

    /**
     * This will delete all 'png' files in the screenshot directory belonging to
     * this {@link ScreenshotRecorder}. If the directory is empty as an result
     * of this operation the directory will be removed as well.
     *
     * @return true if the screenshot directory was removed or didn't exist in
     *         the first place
     */
    public boolean deleteRecordedScreenshots() throws IOException {
        if (_screenshotDirectory.exists()) {
            for (String name : _screenshotDirectory
                    .list(PNGFileNameFilter.INSTANCE)) {
                File imageFile = new File(_screenshotDirectory, name);
                if (!imageFile.delete()) {
                    return false;
                }
            }
            return _screenshotDirectory.delete();
        }
        return true;
    }

    // ---------------------------------------------------------
    // Internal stuff
    File generateScreenshotFileName(final String name) {
        String fileName = String.format("%s-%s.png", ScreenshotCounter
                .incrementAndGet(), name);
        fileName = fileName.replace("/", "<slash>");
        fileName = fileName.replace("\\", "<backslash>");
        if (fileName.length() > _maxFileNameLength) {
            fileName = fileName.substring(0, _maxFileNameLength);
        }
        if (!_screenshotDirectory.exists()) {
            _screenshotDirectory.mkdirs();
        }
        return new File(_screenshotDirectory, fileName);
    }

    static class PNGFileNameFilter implements FilenameFilter {
        static PNGFileNameFilter INSTANCE = new PNGFileNameFilter();

        public boolean accept(File dir, String name) {
            return name.endsWith(".png");
        }
    }
}
