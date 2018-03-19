package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.AbstractHollowUniqueKeyIndex;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class TranslatedTextPrimaryKeyIndex extends AbstractHollowUniqueKeyIndex<VMSHollowInputAPI, TranslatedTextHollow> {

    public TranslatedTextPrimaryKeyIndex(HollowConsumer consumer) {
        this(consumer, false);    }

    public TranslatedTextPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah) {
        this(consumer, isListenToDataRefreah, ((HollowObjectSchema)consumer.getStateEngine().getSchema("TranslatedText")).getPrimaryKey().getFieldPaths());
    }

    public TranslatedTextPrimaryKeyIndex(HollowConsumer consumer, String... fieldPaths) {
        this(consumer, true, fieldPaths);
    }

    public TranslatedTextPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefreah, String... fieldPaths) {
        super(consumer, "TranslatedText", isListenToDataRefreah, fieldPaths);
    }

    public TranslatedTextHollow findMatch(Object... keys) {
        int ordinal = idx.getMatchingOrdinal(keys);
        if(ordinal == -1)
            return null;
        return api.getTranslatedTextHollow(ordinal);
    }

}