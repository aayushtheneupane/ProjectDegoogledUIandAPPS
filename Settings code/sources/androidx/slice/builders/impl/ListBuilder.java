package androidx.slice.builders.impl;

import androidx.slice.builders.ListBuilder;
import java.util.Set;

public interface ListBuilder {
    void addInputRange(ListBuilder.InputRangeBuilder inputRangeBuilder);

    void addRow(ListBuilder.RowBuilder rowBuilder);

    void setColor(int i);

    void setHeader(ListBuilder.HeaderBuilder headerBuilder);

    void setIsError(boolean z);

    void setKeywords(Set<String> set);

    void setTtl(long j);
}
