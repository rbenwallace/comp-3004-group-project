package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.EditFeeFragment;
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Observable;

import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

@SuppressLint("SetTextI18n")
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
    private FeedCardSpecialization specialization;
    private boolean isExpanded;

    private FragmentActivity activity;

    FeedCard(FeedCardType cardType, FeedCardObject object, FeedCtrl controller) {
        this.cardType = cardType;
        this.object = object;
        this.controller = controller;
        this.activity = controller.activity;
        if (cardType == FeedCardType.FILTER) {
            specialization = new FilterCard();
        } else if (cardType == FeedCardType.DATE) {
            specialization = new DateCard();
        } else if (cardType == FeedCardType.BUBBLE) {
            specialization = new BubbleCard();
        }
        isExpanded = false;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        specialization.attachLayoutViews(layout, cv);
    }

    @Override
    public void updateInfo() {
        specialization.updateInfo();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof FeedCard && ((FeedCard) obj).object.equals(object);
    }

    void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    private void handleClick() {
        isExpanded = !isExpanded;
        updateInfo();
    }

    FeedCardType getCardType() {
        return cardType;
    }

    public FeedCardObject getObject() {
        return object;
    }

    enum FeedCardType {
        BUBBLE, FILTER, DATE
    }

    public interface FeedCardObject extends Observable {

        Date getDueDate();

        Date getDateCompleted();

        void getPerson(Consumer<User> consumer);

        Event getEvent();

    }

    private interface FeedCardSpecialization {
        void attachLayoutViews(View layout, CardView cv);

        void updateInfo();
    }

    private class BubbleCard implements FeedCardSpecialization {
        private View smallView;
        private View largeView;
        private ImageView imgFace;
        private ImageView imgComplete;
        private TextView txtTitle;
        private TextView txtDate;
        private TextView txtName;
        private TextView txtStatus;
        private TextView largeTxtTitle;
        private TextView largeTxtDueDate;
        private TextView largeTxtAssignedTo;
        private TextView largeTxtCompletedDate;
        private TextView largeTxtComplete;
        private boolean isComplete, updatingEvent;

        public void attachLayoutViews(View layout, CardView cv) {
            smallView = layout.findViewById(R.id.feed_card_smallPanel);
            largeView = layout.findViewById(R.id.feed_card_largePanel);
            imgFace = layout.findViewById(R.id.feed_card_imgFace);
            imgComplete = layout.findViewById(R.id.feed_card_imgComplete);
            txtTitle = layout.findViewById(R.id.feed_card_txtTitle);
            txtDate = layout.findViewById(R.id.feed_card_txtDate);
            txtName = layout.findViewById(R.id.feed_card_txtName);
            txtStatus = layout.findViewById(R.id.feed_Card_txtStatus);
            largeTxtTitle = layout.findViewById(R.id.feed_card_large_txtTitle);
            largeTxtDueDate = layout.findViewById(R.id.feed_card_large_txtDueDate);
            largeTxtAssignedTo = layout.findViewById(R.id.feed_card_large_txtName);
            largeTxtCompletedDate = layout.findViewById(R.id.feed_card_large_txtCompletedDate);
            largeTxtComplete = layout.findViewById(R.id.feed_card_large_txtComplete);

            layout.setOnClickListener(v -> handleClick());
            layout.findViewById(R.id.feed_card_pnlEdit).setOnClickListener(v -> {
                if (updatingEvent) {
                    Toast.makeText(activity, "We are updating this event, please wait a second", Toast.LENGTH_LONG).show();
                    return;
                }
                if(object.getEvent().getType() == 0) {
                    activity.pushFragment(FragmentId.GET(EditTaskFragment.TAG), object.getEvent().getAssociatedTask());
                }
                else{
                    activity.pushFragment(FragmentId.GET(EditFeeFragment.TAG), object.getEvent().getAssociatedTask());
                }
            });
            layout.findViewById(R.id.feed_card_pnlComplete).setOnClickListener(v -> {
                if (updatingEvent) {
                    Toast.makeText(activity, "We are updating this event, please wait a second", Toast.LENGTH_LONG).show();
                    return;
                }
                object.getEvent().setDateCompleted(isComplete ? null : new Date());
                isComplete = !isComplete;
                updatingEvent = true;
                Session.getSession().getDatabase().updateEvent(object.getEvent(), updated -> {
                    updateInfo();
                    updatingEvent = false;
                });
            });

            isComplete = object.getDateCompleted() != null;

            layout.performClick();
            layout.performClick();
        }

        public void updateInfo() {
            if (isExpanded) {
                largeView.setVisibility(View.VISIBLE);
                smallView.setVisibility(View.GONE);
            } else {
                smallView.setVisibility(View.VISIBLE);
                largeView.setVisibility(View.GONE);
            }

            boolean isLate =
                    (object.getDateCompleted() == null ? System.currentTimeMillis() :
                            object.getDateCompleted().getTime()) > object.getDueDate().getTime();

            if (!isExpanded) {
                if(object.getEvent().getType() == 1){
                    Session.getSession().getDatabase().getFee(object.getEvent().getAssociatedTask(), fee -> {
                        txtTitle.setText("$" + fee.getAmount() + " - " + object.getName());
                    });
                }
                else{
                    txtTitle.setText(object.getName());
                }
                txtDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        .format(cardType == FeedCardType.BUBBLE && object.getDateCompleted() != null ? object.getDateCompleted() : object.getDueDate()));
                object.getPerson(person -> txtName.setText(person.getFirstName()));

                txtStatus.setText(isLate ? "Late" : "On Time");
                txtStatus.setTextColor(activity.getColor(
                        isLate ? R.color.feedCardLate : R.color.feedCardOnTime));
                txtStatus.setVisibility(object.getDateCompleted() == null ? View.INVISIBLE : View.VISIBLE);

                imgFace.setImageDrawable(activity.getDrawable(
                        isLate ? R.drawable.icons8_puzzled_80 : R.drawable.icons8_angel_80));
            } else {
                largeTxtTitle.setText(object.getName());
                object.getPerson(person -> largeTxtAssignedTo.setText(person.getName()));
                largeTxtDueDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        .format(object.getDueDate()));
                if (object.getDateCompleted() != null) {
                    largeTxtCompletedDate.setText(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                            .format(object.getDateCompleted()));
                } else {
                    largeTxtCompletedDate.setText("incomplete");
                }
                largeTxtCompletedDate.setTextColor(activity.getColor(
                        isLate ? R.color.feedCardLate : R.color.feedCardOnTime));
                imgComplete.setImageDrawable(activity.getDrawable(
                        isComplete ? R.drawable.icons8_error_80 : R.drawable.icons8_checkmark_80));
                largeTxtComplete.setText(isComplete ? "Incomplete" : "Complete");
            }
        }
    }

    private class FilterCard implements FeedCardSpecialization {
        private View closePanel;
        private View optionsPanel;
        private TextView txtTitle;
        private Button btnAssignedToMe;
        private Button btnAssignedToPerson;
        private Button btnShowLate;
        private Button btnShowOnTime;

        public void attachLayoutViews(View layout, CardView cv) {
            layout.setOnClickListener(v -> handleClick());
            closePanel = layout.findViewById(R.id.feed_filter_close);
            optionsPanel = layout.findViewById(R.id.feed_filter_options);
            txtTitle = layout.findViewById(R.id.feed_filter_txtTitle);
            btnAssignedToMe = layout.findViewById(R.id.feed_filter_btnAssignedToMe);
            btnAssignedToPerson = layout.findViewById(R.id.feed_filter_btnAssignedToPerson);
            btnShowLate = layout.findViewById(R.id.feed_filter_btnShowLate);
            btnShowOnTime = layout.findViewById(R.id.feed_filter_btnShowOnTime);

            btnAssignedToMe.setOnClickListener(v -> {
                ObjectId me = Session.getSession().getLoggedInUser().getId();
                if (Settings.FEED_FILTER_USER.get() == me) {
                    Settings.FEED_FILTER_USER.set(null);
                } else {
                    Settings.FEED_FILTER_USER.set(me);
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

        public void updateInfo() {
            Consumer<User> filterUserConsumer = filterUser -> {

                if (filterUser != null) {
                    System.out.println("*** FEED CARD urs= " + filterUser.getId() + " log= " +
                            Session.getSession().getLoggedInUser().getId());
                } else {
                    System.out.println("*** FEED CARD urs=null log= " +
                            Session.getSession().getLoggedInUser().getId());
                }

                boolean assignedToMe = filterUser != null && filterUser.getId().equals(Session.getSession().getLoggedInUser().getId());
                boolean assignedToPerson = !assignedToMe && filterUser != null;
                boolean showLate = Settings.FEED_SHOW_LATE.get();
                boolean showOnTime = Settings.FEED_SHOW_ON_TIME.get();

                if (isExpanded) {
//                smallView.setLayoutParams(new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    closePanel.setVisibility(View.VISIBLE);
                    optionsPanel.setVisibility(View.VISIBLE);
                    txtTitle.setVisibility(View.GONE);

//                System.out.println("FILTER usr= " + controller.filterUser);

                    btnAssignedToMe.setBackgroundColor(activity.getColor(
                            assignedToMe ? R.color.colorPrimary : R.color.colorInverse));
                    btnAssignedToMe.setTextColor(activity.getColor(
                            assignedToMe ? R.color.colorInverse : R.color.colorTextPrimary));

                    btnAssignedToPerson.setBackgroundColor(activity.getColor(
                            assignedToPerson ? R.color.colorPrimary : R.color.colorInverse));
                    btnAssignedToPerson.setTextColor(activity.getColor(
                            assignedToPerson ? R.color.colorInverse : R.color.colorTextPrimary));

                    btnShowLate.setBackgroundColor(activity.getColor(
                            showLate ? R.color.colorPrimaryLight : R.color.colorInverse));
                    btnShowOnTime.setBackgroundColor(activity.getColor(
                            showOnTime ? R.color.colorPrimaryLight : R.color.colorInverse));

                } else {
//                smallView.setLayoutParams(new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    closePanel.setVisibility(View.GONE);
                    optionsPanel.setVisibility(View.GONE);
                    txtTitle.setVisibility(View.VISIBLE);

                    String title = "";
                    if (assignedToMe || assignedToPerson) {
                        title += "(" + filterUser.getFirstName() + ")   ";
                    }
                    if (showLate == showOnTime) {
                        if (!showLate) {
                            title += "hide late, hide on-time";
                        }
                    } else {
                        title += showLate ? "show late, " : "hide late, ";
                        title += showOnTime ? "show on-time" : "hide on-time";
                    }
                    txtTitle.setText(title);
                }
            };

            if (Settings.FEED_FILTER_USER.get() == null) {
                filterUserConsumer.accept(null);
            } else {
                Session.getSession().getDatabase().getUser(Settings.FEED_FILTER_USER.get(), filterUserConsumer);
            }
        }
    }

    private class DateCard implements FeedCardSpecialization {
        private TextView txtDate;

        public void attachLayoutViews(View layout, CardView cv) {
            txtDate = layout.findViewById(R.id.feed_txtDateMarker);
        }

        public void updateInfo() {
            txtDate.setText(object.getName());
        }
    }
}
