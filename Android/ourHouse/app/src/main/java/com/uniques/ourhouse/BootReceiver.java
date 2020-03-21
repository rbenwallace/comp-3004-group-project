package com.uniques.ourhouse;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    private static final int EVENT_SERVICE_JOB_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_BOOT_COMPLETED))
            throw new AssertionError();
        scheduleJobs(context);
    }

    public static void scheduleJobs(Context context) {
        JobScheduler jobScheduler = Objects.requireNonNull(context.getSystemService(JobScheduler.class));
        if (jobScheduler.getPendingJob(EVENT_SERVICE_JOB_ID) == null) {
            ComponentName serviceComponent = new ComponentName(context, EventService.class);
            JobInfo.Builder builder = new JobInfo.Builder(EVENT_SERVICE_JOB_ID, serviceComponent);

            builder.setPeriodic(EventService.JOB_INTERVAL_MILLIS, EventService.JOB_INTERVAL_FLEX_MILLIS);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            builder.setRequiresDeviceIdle(true);
            builder.setRequiresCharging(false);

            if (jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) {
                Log.d("BootReceiver", "EventService job scheduled");
            } else {
                Log.e("BootReceiver", "EventService job failed to schedule");
            }
        } else {
            Log.d("BootReceiver", "EventService job already scheduled");
        }
    }
}
