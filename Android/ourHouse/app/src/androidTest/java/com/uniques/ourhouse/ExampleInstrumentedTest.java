package com.uniques.ourhouse;

import android.content.Context;

import com.uniques.ourhouse.session.Session;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.uniques.ourhouse", appContext.getPackageName());

        Session.newSession(appContext);
        Session session = Session.getSession();
        session.setDatabase(new EventServiceTest.FakeDatabase());

        EventService service = new EventService();

        service.onStartJob(null);
    }
}
