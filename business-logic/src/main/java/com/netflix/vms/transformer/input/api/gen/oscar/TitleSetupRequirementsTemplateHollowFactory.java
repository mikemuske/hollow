package com.netflix.vms.transformer.input.api.gen.oscar;

import com.netflix.hollow.api.objects.provider.HollowFactory;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.api.custom.HollowTypeAPI;

@SuppressWarnings("all")
public class TitleSetupRequirementsTemplateHollowFactory<T extends TitleSetupRequirementsTemplate> extends HollowFactory<T> {

    @Override
    public T newHollowObject(HollowTypeDataAccess dataAccess, HollowTypeAPI typeAPI, int ordinal) {
        return (T)new TitleSetupRequirementsTemplate(((TitleSetupRequirementsTemplateTypeAPI)typeAPI).getDelegateLookupImpl(), ordinal);
    }

    @Override
    public T newCachedHollowObject(HollowTypeDataAccess dataAccess, HollowTypeAPI typeAPI, int ordinal) {
        return (T)new TitleSetupRequirementsTemplate(new TitleSetupRequirementsTemplateDelegateCachedImpl((TitleSetupRequirementsTemplateTypeAPI)typeAPI, ordinal), ordinal);
    }

}