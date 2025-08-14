package de.julianahrens.datacenter;

import de.julianahrens.strategy.BasicStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Coalition extends ComputeClusterElement implements DataCenterCollection {

    private final List<DataCenter> dataCenters;
    private final List<DataCenterCostTuple> ownDataCenterCosts;

    private final List<DataCenterCostTuple> incomingDelegationRequests = new ArrayList<>();
    private final List<DataCenter> ownDelegationRequests = new ArrayList<>();

    public Coalition(List<DataCenter> _dataCenters, FederatedComputeCluster _cluster) {
        dataCenters = _dataCenters;
        cluster = _cluster;

        dataCenters.forEach(dataCenter -> {
            dataCenter.setCoalition(Optional.of(this));
        });

        collectDelegationCandidates();
        ownDataCenterCosts = getDataCentersByCost();
        ownDataCenterCosts.sort(null);

        ownDataCenterCosts.forEach(this::updateDelegationCandidate);
        delegationCandidates.sort(null);
    }

    @Override
    public List<DataCenterCostTuple> getDataCentersByCost() {
        return dataCenters.stream().map(dataCenter -> new DataCenterCostTuple(dataCenter, dataCenter.getActualCost())).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void doDelegate() {
        ownDelegationRequests.forEach(ownDelegationRequest -> BasicStrategy.instance.doDelegate(ownDelegationRequest, delegationCandidates));
    }

    @Override
    public void doConfirmOrRejectDelegationRequests() {
        if (!incomingDelegationRequests.isEmpty()) {
            sortDescending(incomingDelegationRequests);
            int ownDataCenterIndex = 0;
            for (DataCenterCostTuple delegationRequest : incomingDelegationRequests) {
                if (ownDataCenterCosts.get(ownDataCenterIndex).getDataCenter().doConfirmOrRejectSingleDelegationRequest(delegationRequest)) {
                    // next job should be offered to datacenter with next higher cost
                    if(++ownDataCenterIndex >= ownDataCenterCosts.size()) {
                        break;
                    };
                } else {
                    // if this job was not excepted by this data center the next job which will have a lower value will not be accepted by the next datacenter which will have higher cost either so every following job can be safely rejected by the coalition
                    break;
                }
            }
        }
    }

    public void receiveIncomingDelegationRequest(DataCenterCostTuple delegationRequest) {
        incomingDelegationRequests.add(delegationRequest);
    }

    public void receiveOwnDelegationRequest(DataCenter delegationRequest) {
        ownDelegationRequests.add(delegationRequest);
    }
}
