package android.support.p000v4.widget;

import android.support.p000v4.util.Pools$Pool;
import android.support.p000v4.util.Pools$SimplePool;
import android.support.p000v4.util.SimpleArrayMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* renamed from: android.support.v4.widget.DirectedAcyclicGraph */
public final class DirectedAcyclicGraph<T> {
    private final SimpleArrayMap<T, ArrayList<T>> mGraph = new SimpleArrayMap<>();
    private final Pools$Pool<ArrayList<T>> mListPool = new Pools$SimplePool(10);
    private final ArrayList<T> mSortResult = new ArrayList<>();
    private final HashSet<T> mSortTmpMarked = new HashSet<>();

    private void dfs(T t, ArrayList<T> arrayList, HashSet<T> hashSet) {
        if (!arrayList.contains(t)) {
            if (!hashSet.contains(t)) {
                hashSet.add(t);
                ArrayList arrayList2 = this.mGraph.get(t);
                if (arrayList2 != null) {
                    int size = arrayList2.size();
                    for (int i = 0; i < size; i++) {
                        dfs(arrayList2.get(i), arrayList, hashSet);
                    }
                }
                hashSet.remove(t);
                arrayList.add(t);
                return;
            }
            throw new RuntimeException("This graph contains cyclic dependencies");
        }
    }

    public void addEdge(T t, T t2) {
        boolean z = true;
        if (this.mGraph.indexOfKey(t) >= 0) {
            if (this.mGraph.indexOfKey(t2) < 0) {
                z = false;
            }
            if (z) {
                ArrayList arrayList = this.mGraph.get(t);
                if (arrayList == null) {
                    arrayList = this.mListPool.acquire();
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    this.mGraph.put(t, arrayList);
                }
                arrayList.add(t2);
                return;
            }
        }
        throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
    }

    public void addNode(T t) {
        if (!(this.mGraph.indexOfKey(t) >= 0)) {
            this.mGraph.put(t, null);
        }
    }

    public void clear() {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList valueAt = this.mGraph.valueAt(i);
            if (valueAt != null) {
                valueAt.clear();
                this.mListPool.release(valueAt);
            }
        }
        this.mGraph.clear();
    }

    public boolean contains(T t) {
        return this.mGraph.indexOfKey(t) >= 0;
    }

    public List getIncomingEdges(T t) {
        return this.mGraph.get(t);
    }

    public List<T> getOutgoingEdges(T t) {
        int size = this.mGraph.size();
        ArrayList arrayList = null;
        for (int i = 0; i < size; i++) {
            ArrayList valueAt = this.mGraph.valueAt(i);
            if (valueAt != null && valueAt.contains(t)) {
                if (arrayList == null) {
                    arrayList = new ArrayList();
                }
                arrayList.add(this.mGraph.keyAt(i));
            }
        }
        return arrayList;
    }

    public ArrayList<T> getSortedList() {
        this.mSortResult.clear();
        this.mSortTmpMarked.clear();
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            dfs(this.mGraph.keyAt(i), this.mSortResult, this.mSortTmpMarked);
        }
        return this.mSortResult;
    }

    public boolean hasOutgoingEdges(T t) {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList valueAt = this.mGraph.valueAt(i);
            if (valueAt != null && valueAt.contains(t)) {
                return true;
            }
        }
        return false;
    }
}