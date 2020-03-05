package com.uniques.ourhouse.fragment;

import com.uniques.ourhouse.ActivityId;

import java.util.HashMap;

import androidx.annotation.Nullable;

public final class FragmentId {
    private static final HashMap<String, FragmentId> fragmentIds = new HashMap<>();

    private Class<? extends Fragment> fragmentClass;
    private String name;
    private int layoutId;
    private ActivityId defaultActivityId;
    private boolean isBaseFragment;

    /**
     * Gets the fragment id associated with the supplied name.
     * This is usually found in the fragment implementation (e.g. Fragment.TAG)
     * @param fragmentName name associated with fragmentId
     */
    @Nullable
    public static FragmentId GET(String fragmentName) {
        return fragmentIds.get(fragmentName);
    }

    public static FragmentId SET(Class<? extends Fragment> fragmentClass, String name, int layoutId, ActivityId defaultActivityId) {
       return SET(fragmentClass, name, layoutId, defaultActivityId, false);
    }

    static FragmentId SET(Class<? extends Fragment> fragmentClass, String name, int layoutId, ActivityId defaultActivityId, boolean isBaseFragment) {
        FragmentId fragmentId;
        if (fragmentIds.containsKey(name)) {
            fragmentId = fragmentIds.get(name);
            fragmentId.layoutId = layoutId;
            fragmentId.defaultActivityId = defaultActivityId;
        } else {
            fragmentId = new FragmentId(fragmentClass, name, layoutId, defaultActivityId, isBaseFragment);
            fragmentIds.put(name, fragmentId);
        }
        return fragmentId;
    }

    private FragmentId(Class<? extends Fragment> fragmentClass, String name, int layoutId, ActivityId defaultActivityId, boolean isBaseFragment) {
        this.fragmentClass = fragmentClass;
        this.name = name;
        this.layoutId = layoutId;
        this.defaultActivityId = defaultActivityId;
        this.isBaseFragment = isBaseFragment;
    }

    public Fragment newInstance() throws InstantiationException, IllegalAccessException {
        return fragmentClass.newInstance();
    }

    String getName() {
        return name;
    }

    int getLayoutId() {
        return layoutId;
    }

    ActivityId getDefaultActivityId() {
        return defaultActivityId;
    }

    public boolean isBaseFragment() {
        return isBaseFragment;
    }

    @Override
    public String toString() {
        return name;
    }
}
