package com.android.settings.homepage.contextualcards;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class ContextualCardsDiffCallback extends DiffUtil.Callback {
    private final List<ContextualCard> mNewCards;
    private final List<ContextualCard> mOldCards;

    public ContextualCardsDiffCallback(List<ContextualCard> list, List<ContextualCard> list2) {
        this.mOldCards = list;
        this.mNewCards = list2;
    }

    public int getOldListSize() {
        return this.mOldCards.size();
    }

    public int getNewListSize() {
        return this.mNewCards.size();
    }

    public boolean areItemsTheSame(int i, int i2) {
        return this.mOldCards.get(i).getName().equals(this.mNewCards.get(i2).getName());
    }

    public boolean areContentsTheSame(int i, int i2) {
        if (this.mNewCards.get(i2).hasInlineAction()) {
            return false;
        }
        return this.mOldCards.get(i).equals(this.mNewCards.get(i2));
    }
}
