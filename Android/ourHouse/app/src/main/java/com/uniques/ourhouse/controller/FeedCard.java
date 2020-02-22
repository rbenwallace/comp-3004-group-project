package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Observable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public final class FeedCard implements RecyclerCard, Comparable {

    private static final String DATE_FORMAT = "EE d MMM k:mm a";

    @Override
    public int getCompareType() {
        return DATE;
    }

    @Override
    public Comparable getCompareObject() {
        return object;
    }

    private FeedCardType cardType;
    private FeedCardObject object;
    private FragmentActivity activity;
    private View smallView;
    private View largeView;
    private View closePanel;
    private View optionsPanel;
    private ImageView imgFace;
    private TextView txtTitle;
    private TextView txtDate;
    private TextView txtName;
    private TextView txtStatus;
    private TextView largeTxtTitle;
    private TextView largeTxtDueDate;
    private TextView largeTxtAssignedTo;
    private TextView largeTxtCompletedDate;
    private boolean isExpanded;

    public FeedCard(FeedCardType cardType, FeedCardObject object, FragmentActivity activity) {
        this.cardType = cardType;
        this.object = object;
        this.activity = activity;
        isExpanded = false;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        if (cardType == FeedCardType.FILTER) {
            layout.setOnClickListener(v -> handleClick());
            smallView = layout.findViewById(R.id.feed_filter_parent);
            closePanel = layout.findViewById(R.id.feed_filter_close);
            optionsPanel = layout.findViewById(R.id.feed_filter_options);
        }
        if (cardType == FeedCardType.DATE) {
            txtDate = layout.findViewById(R.id.feed_txtDateMarker);
        }
        if (cardType == FeedCardType.EVENT || cardType == FeedCardType.TASK) {
            layout.setOnClickListener(v -> handleClick());
            smallView = layout.findViewById(R.id.feed_card_smallPanel);
            largeView = layout.findViewById(R.id.feed_card_largePanel);
            imgFace = layout.findViewById(R.id.feed_card_imgFace);
            txtTitle = layout.findViewById(R.id.feed_card_txtTitle);
            txtDate = layout.findViewById(R.id.feed_card_txtDate);
            txtName = layout.findViewById(R.id.feed_card_txtName);
            txtStatus = layout.findViewById(R.id.feed_Card_txtStatus);
            largeTxtTitle = layout.findViewById(R.id.feed_card_large_txtTitle);
            largeTxtDueDate = layout.findViewById(R.id.feed_card_large_txtDueDate);
            largeTxtAssignedTo = layout.findViewById(R.id.feed_card_large_txtName);
            largeTxtCompletedDate = layout.findViewById(R.id.feed_card_large_txtCompletedDate);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
        if (cardType == FeedCardType.FILTER) {
            if (isExpanded) {
//                smallView.setLayoutParams(new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                closePanel.setVisibility(View.VISIBLE);
                optionsPanel.setVisibility(View.VISIBLE);
            } else {
//                smallView.setLayoutParams(new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                closePanel.setVisibility(View.GONE);
                optionsPanel.setVisibility(View.GONE);
            }
        }
        if (cardType == FeedCardType.DATE) {
            txtDate.setText(object.getName());
        }
        if (cardType == FeedCardType.EVENT || cardType == FeedCardType.TASK) {
            if (isExpanded) {
                largeView.setVisibility(View.VISIBLE);
                smallView.setVisibility(View.GONE);
            } else {
                smallView.setVisibility(View.VISIBLE);
                largeView.setVisibility(View.GONE);
            }

            boolean isLate =
                    cardType != FeedCardType.TASK && (object.getDateCompleted() == null ||
                            object.getDateCompleted().getTime() > object.getDueDate().getTime());

            if (isExpanded) {
                txtTitle.setText(object.getName());
                txtDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        .format(cardType == FeedCardType.EVENT && object.getDateCompleted() != null ? object.getDateCompleted() : object.getDueDate()));
                txtName.setText(object.getPerson().getFirstName());

                txtStatus.setText(isLate ? "Late" : "On Time");
                txtStatus.setTextColor(activity.getColor(isLate ? R.color.feedCardLate : R.color.feedCardOnTime));

                imgFace.setImageDrawable(activity.getDrawable(isLate ? R.drawable.icons8_puzzled_80 : R.drawable.icons8_angel_80));
            } else {
                largeTxtTitle.setText(object.getName());
                largeTxtAssignedTo.setText(object.getPerson().getFullName());
                largeTxtDueDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        .format(object.getDueDate()));
                if (object.getDateCompleted() != null) {
                    largeTxtCompletedDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                            .format(object.getDateCompleted()));
                } else {
                    largeTxtCompletedDate.setText("incomplete");
                }
                largeTxtCompletedDate.setTextColor(activity.getColor(isLate ? R.color.feedCardLate : R.color.feedCardOnTime));
            }
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof FeedCard && ((FeedCard) obj).object.equals(object);
    }

    private void handleClick() {
        isExpanded = !isExpanded;
        updateInfo();
    }

    public FeedCardType getCardType() {
        return cardType;
    }

    public FeedCardObject getObject() {
        return object;
    }

    enum FeedCardType {
        EVENT, TASK, FILTER, DATE
    }

    public interface FeedCardObject extends Observable {

        Date getDueDate();

        Date getDateCompleted();

        User getPerson();

    }
}
