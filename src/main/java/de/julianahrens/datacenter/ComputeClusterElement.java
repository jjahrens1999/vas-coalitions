package de.julianahrens.datacenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ComputeClusterElement {

    protected ArrayList<DataCenterCostTuple> delegationCandidates;
    protected FederatedComputeCluster cluster;

    protected void collectDelegationCandidates() {
        delegationCandidates = cluster.getDataCentersByCost();
    }

    protected void updateDelegationCandidate(DataCenterCostTuple candidate) {
        for (int i = 0; i < delegationCandidates.size(); i++) {
            if (delegationCandidates.get(i).getDataCenter().equals(candidate.getDataCenter())) {
                delegationCandidates.set(i, candidate);
            }
        }
    }

    protected <T> void sortDescending(List<T> list) {
        // using null means to use the sorting implemented in the compareTo method of DataCenterCostTuple
        list.sort(null);
        Collections.reverse(list);
    }

    public abstract void doDelegate();
    public abstract void doConfirmOrRejectDelegationRequests();
}
