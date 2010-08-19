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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.thoughtworks.selenium.CommandProcessor;

public class RecordingCommandProcessor implements CommandProcessor {

    @SuppressWarnings("serial")
    private static final Collection<String> IGNORE_COMMANDS_SET =
        Collections.unmodifiableCollection(new HashSet<String>() {{
            add("captureEntirePageScreenshot");
            add("captureScreenshot");
        }});

    private final CommandProcessor _cp;

    public RecordingCommandProcessor(final CommandProcessor cp) {
        this._cp = cp;
    }

    public String doCommand(String command, String[] args) {
        String result = _cp.doCommand(command, args);
        if (shouldBeRecorded()) {
            ScreenshotRecorder recorder = ScreenshotRecorder.current();
            if (recorder != null) {
                recorder.recordScreenshot(_cp, command + "-" + Arrays.asList(args));
            }
        }
        return result;
    }

    private boolean shouldBeRecorded() {
        return !IGNORE_COMMANDS_SET.contains(IGNORE_COMMANDS_SET);
    }

    // ------------------------------------------------------------
    // CommandProcessorDelegation
    // ------------------------------------------------------------
    public boolean getBoolean(String string, String[] strings) {
        return _cp.getBoolean(string, strings);
    }

    public boolean[] getBooleanArray(String string, String[] strings) {
        return _cp.getBooleanArray(string, strings);
    }

    public Number getNumber(String string, String[] strings) {
        return _cp.getNumber(string, strings);
    }

    public Number[] getNumberArray(String string, String[] strings) {
        return _cp.getNumberArray(string, strings);
    }

    public String getRemoteControlServerLocation() {
        return _cp.getRemoteControlServerLocation();
    }

    public String getString(String string, String[] strings) {
        return _cp.getString(string, strings);
    }

    public String[] getStringArray(String string, String[] strings) {
        return _cp.getStringArray(string, strings);
    }

    public void setExtensionJs(String extensionJs) {
        _cp.setExtensionJs(extensionJs);
    }

    public void start() {
        _cp.start();
    }

    public void start(Object optionsObject) {
        _cp.start(optionsObject);
    }

    public void start(String optionsString) {
        _cp.start(optionsString);
    }

    public void stop() {
        _cp.stop();
    }
}
