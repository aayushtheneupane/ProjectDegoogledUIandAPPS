package com.android.systemui.bubbles;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BubbleData {
    private static final Comparator<Bubble> BUBBLES_BY_SORT_KEY_DESCENDING = Comparator.comparing($$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o.INSTANCE).reversed();
    private static final Comparator<Map.Entry<String, Long>> GROUPS_BY_MAX_SORT_KEY_DESCENDING = Comparator.comparing($$Lambda$JmVHPWbzq5woEs3Hauzhf2I3Jc.INSTANCE).reversed();
    private final List<Bubble> mBubbles;
    private final Context mContext;
    private boolean mExpanded;
    private Listener mListener;
    private Bubble mSelectedBubble;
    private Update mStateChange;
    private HashMap<String, String> mSuppressedGroupKeys = new HashMap<>();
    private TimeSource mTimeSource = $$Lambda$0E0fwzH9SS6aB9lL5npMzupI4Q.INSTANCE;
    private NotificationListenerService.Ranking mTmpRanking;

    interface Listener {
        void applyUpdate(Update update);
    }

    interface TimeSource {
        long currentTimeMillis();
    }

    static final class Update {
        Bubble addedBubble;
        final List<Bubble> bubbles;
        boolean expanded;
        boolean expandedChanged;
        boolean orderChanged;
        final List<Pair<Bubble, Integer>> removedBubbles;
        Bubble selectedBubble;
        boolean selectionChanged;
        Bubble updatedBubble;

        private Update(List<Bubble> list) {
            this.removedBubbles = new ArrayList();
            this.bubbles = Collections.unmodifiableList(list);
        }

        /* access modifiers changed from: package-private */
        public boolean anythingChanged() {
            return this.expandedChanged || this.selectionChanged || this.addedBubble != null || this.updatedBubble != null || !this.removedBubbles.isEmpty() || this.orderChanged;
        }

        /* access modifiers changed from: package-private */
        public void bubbleRemoved(Bubble bubble, int i) {
            this.removedBubbles.add(new Pair(bubble, Integer.valueOf(i)));
        }
    }

    public BubbleData(Context context) {
        this.mContext = context;
        this.mBubbles = new ArrayList();
        this.mStateChange = new Update(this.mBubbles);
    }

    public boolean hasBubbles() {
        return !this.mBubbles.isEmpty();
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public boolean hasBubbleWithKey(String str) {
        return getBubbleWithKey(str) != null;
    }

    public Bubble getSelectedBubble() {
        return this.mSelectedBubble;
    }

    public void setExpanded(boolean z) {
        setExpandedInternal(z);
        dispatchPendingChanges();
    }

    public void setSelectedBubble(Bubble bubble) {
        setSelectedBubbleInternal(bubble);
        dispatchPendingChanges();
    }

    /* access modifiers changed from: package-private */
    public void notificationEntryUpdated(NotificationEntry notificationEntry, boolean z) {
        Bubble bubbleWithKey = getBubbleWithKey(notificationEntry.key);
        boolean z2 = false;
        boolean z3 = !notificationEntry.isVisuallyInterruptive || z;
        if (bubbleWithKey == null) {
            bubbleWithKey = new Bubble(this.mContext, notificationEntry);
            bubbleWithKey.setSuppressFlyout(z3);
            doAdd(bubbleWithKey);
            trim();
        } else {
            bubbleWithKey.updateEntry(notificationEntry);
            bubbleWithKey.setSuppressFlyout(z3);
            doUpdate(bubbleWithKey);
        }
        if (bubbleWithKey.shouldAutoExpand()) {
            setSelectedBubbleInternal(bubbleWithKey);
            if (!this.mExpanded) {
                setExpandedInternal(true);
            }
        } else if (this.mSelectedBubble == null) {
            setSelectedBubbleInternal(bubbleWithKey);
        }
        if (this.mExpanded && this.mSelectedBubble == bubbleWithKey) {
            z2 = true;
        }
        bubbleWithKey.setShowInShadeWhenBubble(!z2);
        bubbleWithKey.setShowBubbleDot(!z2);
        dispatchPendingChanges();
    }

    public void notificationEntryRemoved(NotificationEntry notificationEntry, int i) {
        doRemove(notificationEntry.key, i);
        dispatchPendingChanges();
    }

    public void notificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
        if (this.mTmpRanking == null) {
            this.mTmpRanking = new NotificationListenerService.Ranking();
        }
        String[] orderedKeys = rankingMap.getOrderedKeys();
        for (String str : orderedKeys) {
            if (hasBubbleWithKey(str)) {
                rankingMap.getRanking(str, this.mTmpRanking);
                if (!this.mTmpRanking.canBubble()) {
                    doRemove(str, 4);
                }
            }
        }
        dispatchPendingChanges();
    }

    /* access modifiers changed from: package-private */
    public void addSummaryToSuppress(String str, String str2) {
        this.mSuppressedGroupKeys.put(str, str2);
    }

    /* access modifiers changed from: package-private */
    public String getSummaryKey(String str) {
        return this.mSuppressedGroupKeys.get(str);
    }

    /* access modifiers changed from: package-private */
    public void removeSuppressedSummary(String str) {
        this.mSuppressedGroupKeys.remove(str);
    }

    /* access modifiers changed from: package-private */
    public boolean isSummarySuppressed(String str) {
        return this.mSuppressedGroupKeys.containsKey(str);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Bubble> getBubblesInGroup(String str) {
        ArrayList<Bubble> arrayList = new ArrayList<>();
        if (str == null) {
            return arrayList;
        }
        for (Bubble next : this.mBubbles) {
            if (str.equals(next.getEntry().notification.getGroupKey())) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    private void doAdd(Bubble bubble) {
        if (insertBubble((!isExpanded() || (hasBubbleWithGroupId(bubble.getGroupId()) ^ true)) ? 0 : findFirstIndexForGroup(bubble.getGroupId()), bubble) < this.mBubbles.size() - 1) {
            this.mStateChange.orderChanged = true;
        }
        this.mStateChange.addedBubble = bubble;
        if (!isExpanded()) {
            Update update = this.mStateChange;
            update.orderChanged = packGroup(findFirstIndexForGroup(bubble.getGroupId())) | update.orderChanged;
            setSelectedBubbleInternal(this.mBubbles.get(0));
        }
    }

    private void trim() {
        if (this.mBubbles.size() > 5) {
            this.mBubbles.stream().sorted(Comparator.comparingLong($$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY.INSTANCE)).filter(new Predicate() {
                public final boolean test(Object obj) {
                    return BubbleData.this.lambda$trim$0$BubbleData((Bubble) obj);
                }
            }).findFirst().ifPresent(new Consumer() {
                public final void accept(Object obj) {
                    BubbleData.this.lambda$trim$1$BubbleData((Bubble) obj);
                }
            });
        }
    }

    public /* synthetic */ boolean lambda$trim$0$BubbleData(Bubble bubble) {
        return !bubble.equals(this.mSelectedBubble);
    }

    public /* synthetic */ void lambda$trim$1$BubbleData(Bubble bubble) {
        doRemove(bubble.getKey(), 2);
    }

    private void doUpdate(Bubble bubble) {
        this.mStateChange.updatedBubble = bubble;
        if (!isExpanded()) {
            int indexOf = this.mBubbles.indexOf(bubble);
            this.mBubbles.remove(bubble);
            int insertBubble = insertBubble(0, bubble);
            if (indexOf != insertBubble) {
                packGroup(insertBubble);
                this.mStateChange.orderChanged = true;
            }
            setSelectedBubbleInternal(this.mBubbles.get(0));
        }
    }

    private void doRemove(String str, int i) {
        int indexForKey = indexForKey(str);
        if (indexForKey != -1) {
            Bubble bubble = this.mBubbles.get(indexForKey);
            if (this.mBubbles.size() == 1) {
                setExpandedInternal(false);
                setSelectedBubbleInternal((Bubble) null);
            }
            if (indexForKey < this.mBubbles.size() - 1) {
                this.mStateChange.orderChanged = true;
            }
            this.mBubbles.remove(indexForKey);
            this.mStateChange.bubbleRemoved(bubble, i);
            if (!isExpanded()) {
                this.mStateChange.orderChanged |= repackAll();
            }
            if (Objects.equals(this.mSelectedBubble, bubble)) {
                setSelectedBubbleInternal(this.mBubbles.get(Math.min(indexForKey, this.mBubbles.size() - 1)));
            }
            maybeSendDeleteIntent(i, bubble.getEntry());
        }
    }

    public void dismissAll(int i) {
        if (!this.mBubbles.isEmpty()) {
            setExpandedInternal(false);
            setSelectedBubbleInternal((Bubble) null);
            while (!this.mBubbles.isEmpty()) {
                Bubble remove = this.mBubbles.remove(0);
                maybeSendDeleteIntent(i, remove.getEntry());
                this.mStateChange.bubbleRemoved(remove, i);
            }
            dispatchPendingChanges();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayEmpty(int i) {
        for (Bubble next : this.mBubbles) {
            if (next.getDisplayId() == i) {
                if (next.getExpandedView() != null) {
                    next.getExpandedView().notifyDisplayEmpty();
                    return;
                }
                return;
            }
        }
    }

    private void dispatchPendingChanges() {
        if (this.mListener != null && this.mStateChange.anythingChanged()) {
            this.mListener.applyUpdate(this.mStateChange);
        }
        this.mStateChange = new Update(this.mBubbles);
    }

    private void setSelectedBubbleInternal(Bubble bubble) {
        if (!Objects.equals(bubble, this.mSelectedBubble)) {
            if (bubble == null || this.mBubbles.contains(bubble)) {
                if (this.mExpanded && bubble != null) {
                    bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
                }
                this.mSelectedBubble = bubble;
                Update update = this.mStateChange;
                update.selectedBubble = bubble;
                update.selectionChanged = true;
                return;
            }
            Log.e("Bubbles", "Cannot select bubble which doesn't exist! (" + bubble + ") bubbles=" + this.mBubbles);
        }
    }

    private void setExpandedInternal(boolean z) {
        if (this.mExpanded != z) {
            if (z) {
                if (this.mBubbles.isEmpty()) {
                    Log.e("Bubbles", "Attempt to expand stack when empty!");
                    return;
                }
                Bubble bubble = this.mSelectedBubble;
                if (bubble == null) {
                    Log.e("Bubbles", "Attempt to expand stack without selected bubble!");
                    return;
                }
                bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
                this.mStateChange.orderChanged |= repackAll();
            } else if (!this.mBubbles.isEmpty()) {
                this.mStateChange.orderChanged |= repackAll();
                if (this.mBubbles.indexOf(this.mSelectedBubble) > 0) {
                    if (this.mSelectedBubble.isOngoing() || !this.mBubbles.get(0).isOngoing()) {
                        this.mBubbles.remove(this.mSelectedBubble);
                        this.mBubbles.add(0, this.mSelectedBubble);
                        Update update = this.mStateChange;
                        update.orderChanged = packGroup(0) | update.orderChanged;
                    } else {
                        setSelectedBubbleInternal(this.mBubbles.get(0));
                    }
                }
            }
            this.mExpanded = z;
            Update update2 = this.mStateChange;
            update2.expanded = z;
            update2.expandedChanged = true;
        }
    }

    /* access modifiers changed from: private */
    public static long sortKey(Bubble bubble) {
        long lastUpdateTime = bubble.getLastUpdateTime();
        return bubble.isOngoing() ? lastUpdateTime | 4611686018427387904L : lastUpdateTime;
    }

    private int insertBubble(int i, Bubble bubble) {
        long sortKey = sortKey(bubble);
        String str = null;
        while (i < this.mBubbles.size()) {
            Bubble bubble2 = this.mBubbles.get(i);
            String groupId = bubble2.getGroupId();
            if (!(!groupId.equals(str)) || sortKey <= sortKey(bubble2)) {
                i++;
                str = groupId;
            } else {
                this.mBubbles.add(i, bubble);
                return i;
            }
        }
        this.mBubbles.add(bubble);
        return this.mBubbles.size() - 1;
    }

    private boolean hasBubbleWithGroupId(String str) {
        return this.mBubbles.stream().anyMatch(new Predicate(str) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return ((Bubble) obj).getGroupId().equals(this.f$0);
            }
        });
    }

    private int findFirstIndexForGroup(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            if (this.mBubbles.get(i).getGroupId().equals(str)) {
                return i;
            }
        }
        return 0;
    }

    private boolean packGroup(int i) {
        String groupId = this.mBubbles.get(i).getGroupId();
        ArrayList arrayList = new ArrayList();
        for (int size = this.mBubbles.size() - 1; size > i; size--) {
            if (this.mBubbles.get(size).getGroupId().equals(groupId)) {
                arrayList.add(0, this.mBubbles.get(size));
            }
        }
        if (arrayList.isEmpty()) {
            return false;
        }
        this.mBubbles.removeAll(arrayList);
        this.mBubbles.addAll(i + 1, arrayList);
        return true;
    }

    private boolean repackAll() {
        if (this.mBubbles.isEmpty()) {
            return false;
        }
        HashMap hashMap = new HashMap();
        for (Bubble next : this.mBubbles) {
            long longValue = ((Long) hashMap.getOrDefault(next.getGroupId(), 0L)).longValue();
            long sortKey = sortKey(next);
            if (sortKey > longValue) {
                hashMap.put(next.getGroupId(), Long.valueOf(sortKey));
            }
        }
        ArrayList arrayList = new ArrayList(this.mBubbles.size());
        for (String r3 : (List) hashMap.entrySet().stream().sorted(GROUPS_BY_MAX_SORT_KEY_DESCENDING).map($$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk.INSTANCE).collect(Collectors.toList())) {
            this.mBubbles.stream().filter(new Predicate(r3) {
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return ((Bubble) obj).getGroupId().equals(this.f$0);
                }
            }).sorted(BUBBLES_BY_SORT_KEY_DESCENDING).forEachOrdered(new Consumer(arrayList) {
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    this.f$0.add((Bubble) obj);
                }
            });
        }
        if (arrayList.equals(this.mBubbles)) {
            return false;
        }
        this.mBubbles.clear();
        this.mBubbles.addAll(arrayList);
        return true;
    }

    private void maybeSendDeleteIntent(int i, NotificationEntry notificationEntry) {
        if (i == 1) {
            Notification.BubbleMetadata bubbleMetadata = notificationEntry.getBubbleMetadata();
            PendingIntent deleteIntent = bubbleMetadata != null ? bubbleMetadata.getDeleteIntent() : null;
            if (deleteIntent != null) {
                try {
                    deleteIntent.send();
                } catch (PendingIntent.CanceledException unused) {
                    Log.w("Bubbles", "Failed to send delete intent for bubble with key: " + notificationEntry.key);
                }
            }
        }
    }

    private int indexForKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            if (this.mBubbles.get(i).getKey().equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public List<Bubble> getBubbles() {
        return Collections.unmodifiableList(this.mBubbles);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public Bubble getBubbleWithKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            Bubble bubble = this.mBubbles.get(i);
            if (bubble.getKey().equals(str)) {
                return bubble;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void setTimeSource(TimeSource timeSource) {
        this.mTimeSource = timeSource;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("selected: ");
        Bubble bubble = this.mSelectedBubble;
        printWriter.println(bubble != null ? bubble.getKey() : "null");
        printWriter.print("expanded: ");
        printWriter.println(this.mExpanded);
        printWriter.print("count:    ");
        printWriter.println(this.mBubbles.size());
        for (Bubble dump : this.mBubbles) {
            dump.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.print("summaryKeys: ");
        printWriter.println(this.mSuppressedGroupKeys.size());
        for (String str : this.mSuppressedGroupKeys.keySet()) {
            printWriter.println("   suppressing: " + str);
        }
    }
}
