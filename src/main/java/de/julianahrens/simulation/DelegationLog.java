package de.julianahrens.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelegationLog {

    public static DelegationLog instance = new DelegationLog();

    private final List<Delegation> delegations = new ArrayList<>();

    private DelegationLog() {}

    public void add(Delegation delegation) {
        delegations.add(delegation);
    }

    public void clear() {
        delegations.clear();
    }

    @Override
    public String toString() {
        return delegations.stream().map(Delegation::toString).collect(Collectors.joining(", "));
    }
}
