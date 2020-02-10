package com.uniques.ourhouse.controller;

<<<<<<< HEAD
=======
import android.content.Context;
>>>>>>> master
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public final class RecyclerAdapter<T extends RecyclerCard> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
<<<<<<< HEAD
    private ViewSelector viewSelector;
    private int animationId = -1;
    private RecyclerView recyclerView;
    private List<T> cards;
    private int lastPosition = -1;

    public RecyclerAdapter(RecyclerView recyclerView, List<T> cards, int layoutId) {
        this.recyclerView = recyclerView;
        this.cards = cards;
        this.viewSelector = new ViewSelector() {
=======
    private CardViewSelector cardViewSelector;
    private int animationId;
    private Context context;
    private List<T> cards;
    private int lastPosition = -1;

    public RecyclerAdapter(Context context, List<T> cards, int layoutId, int cardViewId, int animationId) {
        this.context = context;
        this.cards = cards;
        this.cardViewSelector = new CardViewSelector() {
>>>>>>> master
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                return layoutId;
            }
<<<<<<< HEAD
        };
    }

    public RecyclerAdapter(RecyclerView recyclerView, List<T> cards, int layoutId, int animationId) {
        this.recyclerView = recyclerView;
        this.cards = cards;
        this.viewSelector = new ViewSelector() {
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                return layoutId;
=======

            @Override
            public int getCardViewId(int itemViewType) {
                return cardViewId;
>>>>>>> master
            }
        };
        this.animationId = animationId;
    }

<<<<<<< HEAD
    public interface ViewSelector {
=======
    public interface CardViewSelector {
>>>>>>> master
        int getItemViewType(RecyclerCard card);

        int getViewLayoutId(int itemViewType);

<<<<<<< HEAD
        default int getCardViewId(int itemViewType) {
            return -1;
        }
=======
        int getCardViewId(int itemViewType);
>>>>>>> master
    }

    private class CardViewHolder extends RecyclerView.ViewHolder {
        RecyclerCard card;
        View itemView;

        CardViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        void setCard(RecyclerCard card) {
            this.card = card;
            View cv = itemView.findViewById(
<<<<<<< HEAD
                    viewSelector.getCardViewId(viewSelector.getItemViewType(card)));
=======
                    cardViewSelector.getCardViewId(cardViewSelector.getItemViewType(card)));
>>>>>>> master
            this.card.attachLayoutViews(itemView, cv instanceof CardView ? (CardView) cv : null);
        }

        void updateInfo() {
            card.updateInfo();
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
<<<<<<< HEAD
        return viewSelector.getItemViewType(cards.get(position));
=======
        return cardViewSelector.getItemViewType(cards.get(position));
>>>>>>> master
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new CardViewHolder(LayoutInflater.from(viewGroup.getContext())
<<<<<<< HEAD
                .inflate(viewSelector.getViewLayoutId(viewType), viewGroup, false));
=======
                .inflate(cardViewSelector.getViewLayoutId(viewType), viewGroup, false));
>>>>>>> master
    }

    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        cardViewHolder.setCard(cards.get(position));
        cardViewHolder.updateInfo();
        setAnimation(viewHolder.itemView, position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
<<<<<<< HEAD
        if (animationId >= 0 && position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    recyclerView.getContext(), animationId);
=======
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    context, animationId);
>>>>>>> master
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

<<<<<<< HEAD
    void setViewSelector(ViewSelector viewSelector) {
        this.viewSelector = viewSelector;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
=======
    void setCardViewSelector(CardViewSelector cardViewSelector) {
        this.cardViewSelector = cardViewSelector;
>>>>>>> master
    }
}
