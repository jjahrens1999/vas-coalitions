package de.julianahrens.strategy;

import de.julianahrens.datacenter.DataCenter;
import de.julianahrens.datacenter.DataCenterCostTuple;

import java.util.List;

public class BasicStrategy {

    public static final BasicStrategy instance = new BasicStrategy();

    private BasicStrategy() {}

    // implementation of delegation strategy discussed in class
    // data center with highest cost delegates to data center with lowest cost, data center with second-highest cost to data center with second-lowest cost and so on and so forth
    public void doDelegate(DataCenter self, List<DataCenterCostTuple> others) {
        int ownPosition = -1;
        for (int i = 0; i < others.size(); i++) {
            if (others.get(i).getDataCenter().equals(self)) {
                ownPosition = i;
                break;
            }
        }

        if (ownPosition >= others.size() / 2) {
            int targetPosition = others.size() - (ownPosition + 1);
            DataCenter other = others.get(targetPosition).getDataCenter();
            other.receiveDelegationRequest(new DataCenterCostTuple(self, self.getActualCost()));
        }
    }
}
