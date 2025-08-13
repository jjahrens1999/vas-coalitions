package de.julianahrens.datacenter;

public class DataCenterCostTuple implements Comparable<DataCenterCostTuple> {

    private final DataCenter dataCenter;
    private final double cost;

    public DataCenterCostTuple(DataCenter _dataCenter, Double _cost) {
        dataCenter = _dataCenter;
        cost = _cost;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public int compareTo(DataCenterCostTuple other) {
        double diff = cost - other.cost;
        if (diff < 0) return -1;
        else if (diff > 0) return 1;
        return 0;
    }
}
