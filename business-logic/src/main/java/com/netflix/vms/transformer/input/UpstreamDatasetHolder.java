package com.netflix.vms.transformer.input;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.netflix.config.NetflixConfiguration;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.core.util.SimultaneousExecutor;
import com.netflix.vms.transformer.common.input.InputState;
import com.netflix.vms.transformer.common.input.UpstreamDataset;
import com.netflix.vms.transformer.common.slice.InputDataSlicer;
import com.netflix.vms.transformer.hollowinput.VMSHollowInputAPI;
import com.netflix.vms.transformer.input.api.gen.gatekeeper2.Gk2StatusAPI;
import com.netflix.vms.transformer.input.datasets.ConverterDataset;
import com.netflix.vms.transformer.input.datasets.Gatekeeper2Dataset;
import com.netflix.vms.transformer.input.datasets.slicers.ConverterDataSlicerImpl;
import com.netflix.vms.transformer.input.datasets.slicers.Gk2StatusDataSlicerImpl;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UpstreamDatasetHolder {

    public static final String INPUT_VERSION_KEY_PREFIX = "input.cinder.version.";

    private final Map<Dataset, UpstreamDataset> inputs;

    private UpstreamDatasetHolder() {
        this.inputs = new ConcurrentHashMap<>();
    }

    /**
     * Input datasets are enumerated here.
     */
    public enum Dataset {

        CONVERTER(ConverterDataset.class, VMSHollowInputAPI.class, ConverterDataSlicerImpl.class),
        GATEKEEPER2(Gatekeeper2Dataset.class, Gk2StatusAPI.class, Gk2StatusDataSlicerImpl.class);

        private Class<? extends UpstreamDataset> upstream;
        private Class<? extends HollowAPI> api;
        private Class<? extends InputDataSlicer> slicer;
        public static final Map<Dataset, String> PROD_INPUT_NAMESPACES = new EnumMap<>(Dataset.class);
        public static final Map<Dataset, String> TEST_INPUT_NAMESPACES = new EnumMap<>(Dataset.class);

        static {
            PROD_INPUT_NAMESPACES.put(CONVERTER, "vmsconverter-muon");
            PROD_INPUT_NAMESPACES.put(GATEKEEPER2, "gatekeeper2_status_prod");

            TEST_INPUT_NAMESPACES.put(CONVERTER, "vmsconverter-muon");
            TEST_INPUT_NAMESPACES.put(GATEKEEPER2, "gatekeeper2_status_test");
        }

        Dataset(Class<? extends UpstreamDataset> upstream, Class<? extends HollowAPI> api, Class<? extends InputDataSlicer> slicer) {
            this.upstream = upstream;
            this.api = api;
            this.slicer = slicer;
        }

        public Class<? extends UpstreamDataset> getUpstream() {
            return upstream;
        }

        public Class<? extends HollowAPI> getAPI() {
            return api;
        }

        public Class<? extends InputDataSlicer> getSlicer() {
            return slicer;
        }
    }

    public static class UpstreamDatasetConfig {
        public static Map<Dataset, String> getNamespaces() {
            return getNamespacesforEnv(NetflixConfiguration.getEnvironmentEnum() == NetflixConfiguration.EnvironmentEnum.prod);
        }

        // For tooling that seeks test/prod data from dev boxes
        public static Map<Dataset, String> getNamespacesforEnv(boolean isProd) {
            if(isProd)
                return Dataset.PROD_INPUT_NAMESPACES;
            else
                return Dataset.TEST_INPUT_NAMESPACES;
        }

        public static Dataset lookupDatasetForNamespace(String namespace, boolean isProd) {
            final BiMap<Dataset, String> namespaces = HashBiMap.create(getNamespacesforEnv(isProd));
            return namespaces.inverse().get(namespace);
        }

        /**
         * Returns a the key used to identify input version attributes in the transformer output header and metadata.
         * There is an entry for every Cinder input into the transformer. The key contains Cinder namespace which itself
         * may contain dot characters.
         * @param dataset The dataset for which input version attribute key is to be computed.
         * @return input version attribute key. For eg.  "input.cinder.version.vmsconverter-muon".
         */
        public static String getInputVersionAttribute(Dataset dataset) {
            return INPUT_VERSION_KEY_PREFIX + getNamespaces().get(dataset);
        }
    }

    /**
     * Returns a new {@code UpstreamDataHolder} containing inputs
     */
    public static UpstreamDatasetHolder getNewDatasetHolder(Map<Dataset, InputState> inputs) throws Exception {

        SimultaneousExecutor executor = new SimultaneousExecutor(Runtime.getRuntime().availableProcessors(), UpstreamDatasetHolder.class.getName());

        UpstreamDatasetHolder upstreamDatasetHolder = new UpstreamDatasetHolder();

        for (Dataset dataset : Dataset.values()) {
            Class datasetClasz = dataset.getUpstream();
            Constructor datasetConstructor = datasetClasz.getConstructor(new Class[]{InputState.class});
            executor.execute(() -> {
                try {
                    if (inputs.get(dataset) != null) {
                        upstreamDatasetHolder.setDataset(dataset, (UpstreamDataset) datasetConstructor.newInstance(inputs.get(dataset)));
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to instantiate upstream data holder for input " + dataset);
                }
            });
        }

        try {
            executor.awaitSuccessfulCompletion();
        } catch(Throwable th) {
            throw new RuntimeException("Failed to index updated input state into upstream dataset holder", th);
        }

        return upstreamDatasetHolder;
    }


    public void setDataset(Dataset key, UpstreamDataset dataset) {
        this.inputs.put(key, dataset);
    }

    @SuppressWarnings("unchecked")
    public <T extends UpstreamDataset> T getDataset(Dataset dataset) {
        return (T)inputs.get(dataset);
    }
}
