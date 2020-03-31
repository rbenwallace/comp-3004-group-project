package com.uniques.ourhouse.fragment;

import android.util.Log;

import com.uniques.ourhouse.controller.FragmentCtrl;

public abstract class Fragment<T extends FragmentCtrl> extends androidx.fragment.app.Fragment {
    protected T controller;
    private Object[] arguments;

    public T getController() {
        return controller;
    }

    public void setController(T controller) {
        if (this.controller != null) {
            Log.d(getFragmentId().getName(), "Destroying old controller");
            this.controller.onDestroy();
        }
        this.controller = controller;
        Log.d(getFragmentId().getName(), "Controller set");
        if (arguments != null) {
            Log.d(getFragmentId().getName(), "Forwarding arguments");
            this.controller.acceptArguments(arguments);
            arguments = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    public void destroy() {
        Log.d(getFragmentId().getName(), "Destroying self");
        if (controller != null) {
            Log.d(getFragmentId().getName(), "Destroying controller");
            controller.onDestroy();
            controller = null;
        }
    }

    public abstract FragmentId getFragmentId();

    public void offerArguments(Object... args) {
        arguments = args;
    }

    /**
     * @return true if this fragment overrides default behaviour, false otherwise
     */
    public abstract boolean onHomeUpPressed();

    /**
     * @return true if this fragment overrides default behaviour, false otherwise
     */
    public abstract boolean onBackPressed();
}
