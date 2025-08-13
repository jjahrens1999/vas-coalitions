package de.julianahrens.datacenter;

import java.util.Collections;
import java.util.List;

public abstract class ComputeClusterElement {

    protected List<DataCenterCostTuple> delegationCandidates;
    protected FederatedComputeCluster cluster;

    protected void collectDelegationCandidates() {
        delegationCandidates = cluster.getDataCentersByCost();
    }

    protected <T> void sortDescending(List<T> list) {
        // using null means to use the sorting implemented in the compareTo method of DataCenterCostTuple
        list.sort(null);
        Collections.reverse(list);
    }

    public abstract void doDelegate();
    public abstract void doConfirmOrRejectDelegationRequests();
}
