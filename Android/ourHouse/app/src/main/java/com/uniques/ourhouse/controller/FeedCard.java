package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
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

    private FeedCtrl controller;
    private FeedCardType cardType;
    private FeedCardObject object;
    private boolean isExpanded;

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
    private Button btnAssignedToMe;
    private Button btnAssignedToPerson;
    private Button btnShowLate;
    private Button btnShowOnTime;

    public FeedCard(FeedCardType cardType, FeedCardObject object, FeedCtrl controller) {
        this.cardType = cardType;
        this.object = object;
        this.controller = controller;
        this.activity = controller.activity;
        isExpanded = false;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        if (cardType == FeedCardType.FILTER) {
            layout.setOnClickListener(v -> handleClick());
            smallView = layout.findViewById(R.id.feed_filter_parent);
            closePanel = layout.findViewById(R.id.feed_filter_close);
            optionsPanel = layout.findViewById(R.id.feed_filter_options);
            btnAssignedToMe = layout.findViewById(R.id.feed_filter_btnAssignedToMe);
            btnAssignedToPerson = layout.findViewById(R.id.feed_filter_btnAssignedToPerson);
            btnShowLate = layout.findViewById(R.id.feed_filter_btnShowLate);
            btnShowOnTime = layout.findViewById(R.id.feed_filter_btnShowOnTime);

            btnAssignedToMe.setOnClickListener(v -> {
                User me = Session.getSession().getLoggedInUser();
                if (controller.filterUser == me) {
                    controller.filterUser = null;
                } else {
                    controller.filterUser = me;
                }
                controller.updateInfoAndReopenFilter();
            });
            btnAssignedToPerson.setOnClickListener(v -> {

            });
            btnShowLate.setOnClickListener(v -> {
                Settings.FEED_SHOW_LATE.set(!Settings.FEED_SHOW_LATE.<Boolean>get());
                controller.updateInfoAndReopenFilter();
            });
            btnShowOnTime.setOnClickListener(v -> {
                Settings.FEED_SHOW_ON_TIME.set(!Settings.FEED_SHOW_ON_TIME.<Boolean>get());
                controller.updateInfoAndReopenFilter();
            });
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

                boolean assignedToMe = controller.filterUser == Session.getSession().getLoggedInUser();
                boolean assignedToPerson = !assignedToMe && controller.filterUser == null;

                btnAssignedToMe.setBackgroundColor(activity.getColor(
                        assignedToMe ? R.color.colorPrimary : R.color.colorInverse));
                btnAssignedToMe.setTextColor(activity.getColor(
                        assignedToMe ? R.color.colorInverse : R.color.colorTextPrimary));

                btnAssignedToMe.setBackgroundColor(activity.getColor(
                        assignedToPerson ? R.color.colorInverse : R.color.colorPrimary));
                btnAssignedToMe.setTextColor(activity.getColor(
                        assignedToPerson ? R.color.colorTextPrimary : R.color.colorInverse));

                btnShowLate.setBackgroundColor(activity.getColor(
                        Settings.FEED_SHOW_LATE.get() ? R.color.colorPrimaryLight : R.color.colorInverse));
                btnShowOnTime.setBackgroundColor(activity.getColor(
                        Settings.FEED_SHOW_ON_TIME.get() ? R.color.colorPrimaryLight : R.color.colorInverse));
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

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
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
