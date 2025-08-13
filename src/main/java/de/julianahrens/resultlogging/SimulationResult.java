package de.julianahrens.resultlogging;

import java.util.List;
import java.util.Set;

public class SimulationResult {

    private final List<Double> actualCosts;
    private final List<Double> profits;
    private final List<Set<Integer>> coalitionConfiguration;
    private final String delegations;

    public SimulationResult(List<Double> _actualCosts, List<Double> _profits, List<Set<Integer>> _coalitionConfiguration, String _delegations) {
        actualCosts = _actualCosts;
        profits = _profits;
        coalitionConfiguration = _coalitionConfiguration;
        delegations = _delegations;
    }

    public List<Double> getActualCosts() {
        return actualCosts;
    }

    public List<Double> getProfits() {
        return profits;
    }

    public List<Set<Integer>> getCoalitionConfiguration() {
        return coalitionConfiguration;
    }

    public String getDelegations() {
        return delegations;
    }
}
