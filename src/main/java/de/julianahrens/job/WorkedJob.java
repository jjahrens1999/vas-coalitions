package de.julianahrens.job;

import de.julianahrens.simulation.Constants;

public class WorkedJob implements Job {

    private final double price;

    public WorkedJob(double _value) {
        price = _value;
    }

    public WorkedJob(double _originalPrice, double _delegatePrice) {
        // Add the share of the surplus profit owed to the delegating data center to the "price" in order to deduct it from the revenue
        price = (_originalPrice - _delegatePrice) * Constants.ALPHA + _delegatePrice;
    }

    @Override
    public double calculateRevenue() {
        return Constants.JOB_REWARD - price;
    }
}
