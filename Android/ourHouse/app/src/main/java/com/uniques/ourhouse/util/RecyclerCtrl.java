package com.uniques.ourhouse.util;

import com.uniques.ourhouse.controller.RecyclerAdapter;
import com.uniques.ourhouse.controller.RecyclerCard;

public interface RecyclerCtrl<T extends RecyclerCard> {

    void setRecyclerAdapter(RecyclerAdapter<T> recyclerAdapter);
}
