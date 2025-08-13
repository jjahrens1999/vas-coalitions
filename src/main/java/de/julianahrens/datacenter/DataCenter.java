package de.julianahrens.datacenter;

import de.julianahrens.job.DelegatedJob;
import de.julianahrens.job.Job;
import de.julianahrens.job.WorkedJob;
import de.julianahrens.simulation.Delegation;
import de.julianahrens.simulation.DelegationLog;
import de.julianahrens.strategy.BasicStrategy;

import java.util.*;

public class DataCenter extends ComputeClusterElement {

    private final int index;
    private final Double meanCost;
    private final Double actualCost;

    private Optional<Coalition> coalition = Optional.empty();

    private final List<DataCenterCostTuple> delegationRequests = new ArrayList<>();
    private final Map<DataCenter, Job> jobs = new HashMap<>();

    public DataCenter(int _index, FederatedComputeCluster _cluster, Double _meanCost, Double _actualCost) {
        index = _index;
        cluster = _cluster;
        meanCost = _meanCost;
        actualCost = _actualCost;

        jobs.put(this, new WorkedJob(actualCost));
    }

    @Override
    public void doDelegate() {
        if(coalition.isEmpty()) {
            collectDelegationCandidates();
            delegationCandidates.sort(null);
            BasicStrategy.instance.doDelegate(this, delegationCandidates);
        } else {
            // job delegation completely handled by coalition
            coalition.get().receiveOwnDelegationRequest(this);
        }
    }

    @Override
    public void doConfirmOrRejectDelegationRequests() {
        if(!delegationRequests.isEmpty()) {
            sortDescending(delegationRequests);
            doConfirmOrRejectSingleDelegationRequest(delegationRequests.getFirst());
        }
    }

    public boolean doConfirmOrRejectSingleDelegationRequest(DataCenterCostTuple delegationRequest) {
        DataCenter other = delegationRequest.getDataCenter();
        double otherCost = delegationRequest.getCost();
        if(otherCost > actualCost) {
            jobs.put(other, new WorkedJob(otherCost, actualCost));
            other.receiveDelegationConfirmation(this);
            return true;
        } else {
            return false;
        }
    }

    public void receiveDelegationRequest(DataCenterCostTuple delegationRequest) {
        if(coalition.isEmpty()) {
            delegationRequests.add(delegationRequest);
        } else {
            // delegation request handling completely handled by coalition
            coalition.get().receiveIncomingDelegationRequest(delegationRequest);
        }
    }

    public void receiveDelegationConfirmation(DataCenter other) {
        jobs.put(this, new DelegatedJob(actualCost, other.getActualCost()));
        DelegationLog.instance.add(new Delegation(index, other.getIndex()));
    }

    public double calculateProfit() {
        return jobs.values().stream().map(Job::calculateRevenue).reduce(Double::sum).orElse(0.0);
    }

    public void setCoalition(Optional<Coalition> coalition) {
        this.coalition = coalition;
    }

    public int getIndex() {
        return index;
    }

    public Double getActualCost() {
        return actualCost;
    }

    public Double getMeanCost() {
        return meanCost;
    }
}
