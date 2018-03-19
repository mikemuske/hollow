package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.AbstractHollowUniqueKeyIndex;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class PassthroughDataPrimaryKeyIndex extends AbstractHollowUniqueKeyIndex<VMSHollowInputAPI, PassthroughDataHollow> {

    public PassthroughDataPrimaryKeyIndex(HollowConsumer consumer) {
        this(consumer, false);    }

    public PassthroughDataPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah) {
        this(consumer, isListenToDataRefreah, ((HollowObjectSchema)consumer.getStateEngine().getSchema("PassthroughData")).getPrimaryKey().getFieldPaths());
    }

    public PassthroughDataPrimaryKeyIndex(HollowConsumer consumer, String... fieldPaths) {
        this(consumer, true, fieldPaths);
    }

    public PassthroughDataPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah, String... fieldPaths) {
        super(consumer, "PassthroughData", isListenToDataRefreah, fieldPaths);
    }

    public PassthroughDataHollow findMatch(Object... keys) {
        int ordinal = idx.getMatchingOrdinal(keys);
        if(ordinal == -1)
            return null;
        return api.getPassthroughDataHollow(ordinal);
    }

}