package com.android.settings.datetime.timezone;

import android.icu.text.BreakIterator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.datetime.timezone.BaseTimeZoneAdapter.AdapterItem;
import com.android.settings.datetime.timezone.BaseTimeZonePicker;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseTimeZoneAdapter<T extends AdapterItem> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;
    private BaseTimeZoneAdapter<T>.ArrayFilter mFilter;
    private final CharSequence mHeaderText;
    /* access modifiers changed from: private */
    public List<T> mItems;
    /* access modifiers changed from: private */
    public final Locale mLocale;
    private final BaseTimeZonePicker.OnListItemClickListener<T> mOnListItemClickListener;
    /* access modifiers changed from: private */
    public final List<T> mOriginalItems;
    private final boolean mShowHeader;
    private final boolean mShowItemSummary;

    public interface AdapterItem {
        String getCurrentTime();

        String getIconText();

        long getItemId();

        String[] getSearchKeys();

        CharSequence getSummary();

        CharSequence getTitle();
    }

    public BaseTimeZoneAdapter(List<T> list, BaseTimeZonePicker.OnListItemClickListener<T> onListItemClickListener, Locale locale, boolean z, CharSequence charSequence) {
        this.mOriginalItems = list;
        this.mItems = list;
        this.mOnListItemClickListener = onListItemClickListener;
        this.mLocale = locale;
        this.mShowItemSummary = z;
        this.mShowHeader = charSequence != null;
        this.mHeaderText = charSequence;
        setHasStableIds(true);
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater from = LayoutInflater.from(viewGroup.getContext());
        if (i == 0) {
            return new HeaderViewHolder(from.inflate(C1715R.layout.time_zone_search_header, viewGroup, false));
        }
        if (i == 1) {
            return new ItemViewHolder(from.inflate(C1715R.layout.time_zone_search_item, viewGroup, false), this.mOnListItemClickListener);
        }
        throw new IllegalArgumentException("Unexpected viewType: " + i);
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) viewHolder).setText(this.mHeaderText);
        } else if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.setAdapterItem(getDataItem(i));
            itemViewHolder.mSummaryFrame.setVisibility(this.mShowItemSummary ? 0 : 8);
        }
    }

    public long getItemId(int i) {
        if (isPositionHeader(i)) {
            return -1;
        }
        return getDataItem(i).getItemId();
    }

    public int getItemCount() {
        return this.mItems.size() + getHeaderCount();
    }

    public int getItemViewType(int i) {
        return isPositionHeader(i) ^ true ? 1 : 0;
    }

    public final void setHasStableIds(boolean z) {
        super.setHasStableIds(z);
    }

    private int getHeaderCount() {
        return this.mShowHeader ? 1 : 0;
    }

    private boolean isPositionHeader(int i) {
        return this.mShowHeader && i == 0;
    }

    public BaseTimeZoneAdapter<T>.ArrayFilter getFilter() {
        if (this.mFilter == null) {
            this.mFilter = new ArrayFilter();
        }
        return this.mFilter;
    }

    public T getDataItem(int i) {
        return (AdapterItem) this.mItems.get(i - getHeaderCount());
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;

        public HeaderViewHolder(View view) {
            super(view);
            this.mTextView = (TextView) view.findViewById(16908310);
        }

        public void setText(CharSequence charSequence) {
            this.mTextView.setText(charSequence);
        }
    }

    public static class ItemViewHolder<T extends AdapterItem> extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView mIconTextView;
        private T mItem;
        final BaseTimeZonePicker.OnListItemClickListener<T> mOnListItemClickListener;
        final View mSummaryFrame;
        final TextView mSummaryView;
        final TextView mTimeView;
        final TextView mTitleView;

        public ItemViewHolder(View view, BaseTimeZonePicker.OnListItemClickListener<T> onListItemClickListener) {
            super(view);
            view.setOnClickListener(this);
            this.mSummaryFrame = view.findViewById(C1715R.C1718id.summary_frame);
            this.mTitleView = (TextView) view.findViewById(16908310);
            this.mIconTextView = (TextView) view.findViewById(C1715R.C1718id.icon_text);
            this.mSummaryView = (TextView) view.findViewById(16908304);
            this.mTimeView = (TextView) view.findViewById(C1715R.C1718id.current_time);
            this.mOnListItemClickListener = onListItemClickListener;
        }

        public void setAdapterItem(T t) {
            this.mItem = t;
            this.mTitleView.setText(t.getTitle());
            this.mIconTextView.setText(t.getIconText());
            this.mSummaryView.setText(t.getSummary());
            this.mTimeView.setText(t.getCurrentTime());
        }

        public void onClick(View view) {
            this.mOnListItemClickListener.onListItemClick(this.mItem);
        }
    }

    public class ArrayFilter extends Filter {
        private BreakIterator mBreakIterator = BreakIterator.getWordInstance(BaseTimeZoneAdapter.this.mLocale);

        public ArrayFilter() {
        }

        /* access modifiers changed from: protected */
        public Filter.FilterResults performFiltering(CharSequence charSequence) {
            List list;
            if (TextUtils.isEmpty(charSequence)) {
                list = BaseTimeZoneAdapter.this.mOriginalItems;
            } else {
                String lowerCase = charSequence.toString().toLowerCase(BaseTimeZoneAdapter.this.mLocale);
                ArrayList arrayList = new ArrayList();
                for (AdapterItem adapterItem : BaseTimeZoneAdapter.this.mOriginalItems) {
                    String[] searchKeys = adapterItem.getSearchKeys();
                    int length = searchKeys.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String lowerCase2 = searchKeys[i].toLowerCase(BaseTimeZoneAdapter.this.mLocale);
                        if (lowerCase2.startsWith(lowerCase)) {
                            arrayList.add(adapterItem);
                            break;
                        }
                        this.mBreakIterator.setText(lowerCase2);
                        int next = this.mBreakIterator.next();
                        int i2 = 0;
                        while (next != -1) {
                            if (this.mBreakIterator.getRuleStatus() != 0 && lowerCase2.startsWith(lowerCase, i2)) {
                                arrayList.add(adapterItem);
                                break;
                            }
                            i2 = next;
                            next = this.mBreakIterator.next();
                        }
                        i++;
                    }
                }
                list = arrayList;
            }
            Filter.FilterResults filterResults = new Filter.FilterResults();
            filterResults.values = list;
            filterResults.count = list.size();
            return filterResults;
        }

        public void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
            List unused = BaseTimeZoneAdapter.this.mItems = (List) filterResults.values;
            BaseTimeZoneAdapter.this.notifyDataSetChanged();
        }
    }
}
