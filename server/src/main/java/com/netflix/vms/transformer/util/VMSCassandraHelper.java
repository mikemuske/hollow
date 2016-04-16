package com.netflix.vms.transformer.util;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.query.ColumnFamilyQuery;
import com.netflix.astyanax.retry.RetryNTimes;
import com.netflix.astyanax.retry.RetryPolicy;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.cassandra.NFAstyanaxManager;
import com.netflix.vms.transformer.servlet.platform.PlatformLibraries;
import java.util.HashMap;
import java.util.Map;

public class VMSCassandraHelper {

    private final Keyspace keyspace;
    private final ColumnFamily<String, String> columnFamily;

    public VMSCassandraHelper(String clusterName, String keyspaceName, String columnFamilyName) {
        this(PlatformLibraries.ASTYANAX, clusterName, keyspaceName, columnFamilyName);
    }

    public VMSCassandraHelper(NFAstyanaxManager astyanaxManager, String clusterName, String keyspaceName, String columnFamilyName) {
        this.keyspace = getKeyspace(astyanaxManager, clusterName, keyspaceName);
        this.columnFamily = new ColumnFamily<String, String>(columnFamilyName, StringSerializer.get(), StringSerializer.get());
    }


    public void addVipKeyValuePair(String vip, String key, String value) throws ConnectionException {
        addKeyValuePair(vipSpecificKey(vip, key), value);
    }

    public void addKeyValuePair(String key, String value) throws ConnectionException {
        addKeyColumnValue(key, "val", value);
    }

    public void addKeyColumnValue(String key, String columnName, String value) throws ConnectionException {
        MutationBatch batch = createMutationBatch();
        batch.withRow(getColumnFamily(), key).putColumn(columnName, value);
        batch.execute();
    }

    public String getVipKeyValuePair(String vip, String key) throws ConnectionException {
        return getKeyValuePair(vipSpecificKey(vip, key));
    }

    public String getKeyValuePair(String key) throws ConnectionException {
        return getKeyColumnValue(key, "val");
    }

    public String getKeyColumnValue(String key, String columnName) throws ConnectionException {
        return createQuery().getKey(key).getColumn(columnName).execute().getResult().getStringValue();
    }

    public Map<String, String> getColumns(String key) throws ConnectionException {
        OperationResult<ColumnList<String>> opResult = createQuery().getKey(key).execute();

        ColumnList<String> result = opResult.getResult();

        Map<String, String> resultMap = new HashMap<String, String>();

        for(int i=0;i<result.size();i++) {
            Column<String> column = result.getColumnByIndex(i);
            resultMap.put(column.getName(), column.getStringValue());
        }

        return resultMap;
    }

    public MutationBatch createMutationBatch() {
        return this.keyspace.prepareMutationBatch().withConsistencyLevel(ConsistencyLevel.CL_LOCAL_QUORUM)
                .withRetryPolicy(getRetryPolicy());
    }

    public ColumnFamilyQuery<String, String> createQuery() {
        return this.keyspace.prepareQuery(columnFamily).setConsistencyLevel(ConsistencyLevel.CL_LOCAL_QUORUM)
                .withRetryPolicy(getRetryPolicy());
    }

    public ColumnFamily<String, String> getColumnFamily() {
        return columnFamily;
    }

    private RetryPolicy getRetryPolicy() {
        return new RetryNTimes(3);
    }

    public String vipSpecificKey(String vip, String key) {
        return key + "_" + vip;
    }

    private Keyspace getKeyspace(NFAstyanaxManager astyanaxManager, String clusterName, String keyspaceName) {
        Keyspace keyspace = astyanaxManager.getRegisteredKeyspace(clusterName, keyspaceName);
        if (keyspace == null) {
            try {
                keyspace = astyanaxManager.registerKeyspace(clusterName, keyspaceName);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return keyspace;
    }

}
