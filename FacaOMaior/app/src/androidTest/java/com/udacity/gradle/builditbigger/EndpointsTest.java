package com.udacity.gradle.builditbigger;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class EndpointsTest {

    @Test
    public void testVerifyNotEmpty() {
        EndpointsAsyncTask task = new EndpointsAsyncTask() {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                assertFalse(s.isEmpty());
            }
        };
        task.execute();
    }
}