package de.julianahrens.datacenter;

import de.julianahrens.simulation.Constants;
import de.julianahrens.strategy.BasicStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Coalition extends ComputeClusterElement implements DataCenterCollection {

    private final List<DataCenter> dataCenters;
    private final List<DataCenterCostTuple> ownDataCenterCosts;

    private final List<DataCenterCostTuple> incomingDelegationRequests = new ArrayList<>();
    private final List<DataCenterCostTuple> ownDelegationRequests = new ArrayList<>();

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

    private DataCenterCostTuple getDelegationCoalitionUsefulness(DataCenter baseline, DataCenterCostTuple compareTarget) {
        boolean compareTargetInCoalition = dataCenters.contains(compareTarget.getDataCenter());
        if (compareTargetInCoalition) {
            // usefulness to coalition is full additional profit
            return new DataCenterCostTuple(compareTarget.getDataCenter(), compareTarget.getCost() - baseline.getActualCost());
        } else {
            // usefulness to coalition is alpha * additional profit
            return new DataCenterCostTuple(compareTarget.getDataCenter(), (compareTarget.getCost() - baseline.getActualCost()) * Constants.ALPHA);
        }
    }

    // the following function implements to augmented delegation algorithm for coalitions described in the paper
    // as the coalition wants to maximise the collective profit of its members, delegating to data centers in the coalition is preferred
    @Override
    public void doDelegate() {
        List<DataCenterCostTuple> localDelegationCandidates = (ArrayList<DataCenterCostTuple>) delegationCandidates.clone();
        sortDescending(ownDelegationRequests);
        for (DataCenterCostTuple delegationRequest : ownDelegationRequests) {
            List<DataCenterCostTuple> relativeRewards = localDelegationCandidates.stream()
                    .map(candidate -> getDelegationCoalitionUsefulness(delegationRequest.getDataCenter(), candidate))
                    .sorted()
                    .toList();
            Optional<DataCenter> delegationTarget = BasicStrategy.instance.doDelegate(delegationRequest.getDataCenter(), relativeRewards);
            delegationTarget.ifPresent(target -> {
                for (int i = 0; i < localDelegationCandidates.size(); i++) {
                    if (localDelegationCandidates.get(i).getDataCenter().equals(target)) {
                        DataCenterCostTuple currentCandidateProfile = localDelegationCandidates.get(i);
                        // this is done so that no other data center in the coalition that has a price that is less expensive than the currently looked at delegation request also wants to delegate to this target
                        localDelegationCandidates.set(i, new DataCenterCostTuple(target, currentCandidateProfile.getCost() - 100));
                        break;
                    }
                }
            });
        }
    }

    // the following function implements the augmented delegation acceptance algorithm for coalitions described in the paper
    // as the coalition wants to maximise the collective profit of its members, delegation requests from inside the coalition are preferred
    @Override
    public void doConfirmOrRejectDelegationRequests() {
        for (DataCenterCostTuple ownDataCenterCost : ownDataCenterCosts) {
            if (!incomingDelegationRequests.isEmpty()) {
                List<DataCenterCostTuple> incomingDelegationRequestsCoalitionUsefulness = incomingDelegationRequests.stream()
                        .map(delegationRequest -> getDelegationCoalitionUsefulness(ownDataCenterCost.getDataCenter(), delegationRequest))
                        .collect(Collectors.toCollection(ArrayList::new));
                sortDescending(incomingDelegationRequestsCoalitionUsefulness);
                DataCenterCostTuple mostUsefulRequest = incomingDelegationRequestsCoalitionUsefulness.getFirst();
                if (ownDataCenterCost.getDataCenter().doConfirmOrRejectSingleDelegationRequest(mostUsefulRequest.getDataCenter())) {
                    DataCenterCostTuple originalDelegationRequest = incomingDelegationRequests.stream()
                            .filter(delegationRequest -> delegationRequest.getDataCenter().equals(mostUsefulRequest.getDataCenter()))
                            .toList().getFirst();
                    incomingDelegationRequests.remove(originalDelegationRequest);
                } else {
                    // if this job was not accepted by this data center the next data center which will have a higher cost will not accept the next job which will have a lower value either so every following job can be safely rejected by the coalition
                    break;
                }
            }
        }
    }

    public void receiveIncomingDelegationRequest(DataCenterCostTuple delegationRequest) {
        incomingDelegationRequests.add(delegationRequest);
    }

    public void receiveOwnDelegationRequest(DataCenterCostTuple delegationRequest) {
        ownDelegationRequests.add(delegationRequest);
    }
}
