package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.AbstractHollowUniqueKeyIndex;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class StreamAssetTypePrimaryKeyIndex extends AbstractHollowUniqueKeyIndex<VMSHollowInputAPI, StreamAssetTypeHollow> {

    public StreamAssetTypePrimaryKeyIndex(HollowConsumer consumer) {
        this(consumer, false);    }

    public StreamAssetTypePrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah) {
        this(consumer, isListenToDataRefreah, ((HollowObjectSchema)consumer.getStateEngine().getSchema("StreamAssetType")).getPrimaryKey().getFieldPaths());
    }

    public StreamAssetTypePrimaryKeyIndex(HollowConsumer consumer, String... fieldPaths) {
        this(consumer, true, fieldPaths);
    }

    public StreamAssetTypePrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah, String... fieldPaths) {
        super(consumer, "StreamAssetType", isListenToDataRefreah, fieldPaths);
    }

    public StreamAssetTypeHollow findMatch(Object... keys) {
        int ordinal = idx.getMatchingOrdinal(keys);
        if(ordinal == -1)
            return null;
        return api.getStreamAssetTypeHollow(ordinal);
    }

}