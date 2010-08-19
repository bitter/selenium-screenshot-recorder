A small tool for recording selenium browser frames.
==============================================================================
This is a small utility i primarily created to be able to easier debug errors
in selenium tests and communication errors between selenium and the actual 
browser.

The utility dumps a screenshot for every executed selenium command. This may
produce a LOT of files so tread carefully ;)

------------------------------------------------------------------------------
Usage:
    // First of all you should switch from DefaultSelenium to RecordingDefaultSelenium.
    // then you need to update your test to inject a ScreenshotRecorder thread local.
    // One way to do this is described by the following example:

    class MyTestBase extends TestCase {
    
        public void runBare()
            throws Throwable
        {
            ScreenshotRecorder recorder = new ScreenshotRecorder(new File("/tmp"), getName());
            ScreenshotRecorder.set(recorder);
            try {
                super.runBare();
                
            } catch (Throwable t) {
               recorder.packageRecordedScreenshots();
    
            } finally {
                ScreenshotRecorder.set(null);
                recorder.deleteRecordedScreenshots();
            }
        }
    }
