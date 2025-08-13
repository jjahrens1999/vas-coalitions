package de.julianahrens.datacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FederatedComputeCluster implements DataCenterCollection {

    private final List<DataCenter> dataCenters;

    public FederatedComputeCluster(List<DataCenter> _dataCenters) {
        dataCenters = _dataCenters;
    }

    @Override
    public List<DataCenterCostTuple> getDataCentersByCost() {
        return dataCenters.stream().map(
                dataCenter -> new DataCenterCostTuple(dataCenter, dataCenter.getMeanCost())
        ).collect(Collectors.toCollection(ArrayList::new));
    }
}
