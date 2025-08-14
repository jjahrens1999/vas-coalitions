package de.julianahrens.job;

import de.julianahrens.simulation.Constants;

public class DelegatedJob implements Job{

    private final double originalPrice;
    private final double delegatePrice;

    public DelegatedJob(double _originalPrice, double _delegatePrice) {
        originalPrice = _originalPrice;
        delegatePrice = _delegatePrice;
    }

    @Override
    public double calculateRevenue() {
        return (Constants.JOB_REWARD - originalPrice) + (originalPrice - delegatePrice) * Constants.ALPHA;
    }
}
