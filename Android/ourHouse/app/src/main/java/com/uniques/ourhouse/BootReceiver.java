package com.uniques.ourhouse;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    private static final int EVENT_SERVICE_JOB_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_BOOT_COMPLETED))
            throw new AssertionError();

        ComponentName serviceComponent = new ComponentName(context, EventService.class);
        JobInfo.Builder builder = new JobInfo.Builder(EVENT_SERVICE_JOB_ID, serviceComponent);

        builder.setPeriodic(EventService.JOB_INTERVAL_MILLIS, EventService.JOB_INTERVAL_FLEX_MILLIS);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setRequiresDeviceIdle(true);
        builder.setRequiresCharging(false);

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        Objects.requireNonNull(jobScheduler).schedule(builder.build());
    }
}
