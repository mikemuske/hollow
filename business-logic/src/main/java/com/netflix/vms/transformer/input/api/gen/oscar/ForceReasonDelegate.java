package com.netflix.vms.transformer.input.api.gen.oscar;

import com.netflix.hollow.api.objects.delegate.HollowObjectDelegate;


@SuppressWarnings("all")
public interface ForceReasonDelegate extends HollowObjectDelegate {

    public String getValue(int ordinal);

    public boolean isValueEqual(int ordinal, String testValue);

    public ForceReasonTypeAPI getTypeAPI();

}