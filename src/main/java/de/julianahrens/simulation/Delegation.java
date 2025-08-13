package de.julianahrens.simulation;

public class Delegation {

    private final int delegationSender;
    private final int delegationReceiver;

    public Delegation(int _delegationSender, int _delegationReceiver) {
        delegationSender = _delegationSender;
        delegationReceiver = _delegationReceiver;
    }

    @Override
    public String toString() {
        return delegationSender + " -> " + delegationReceiver;
    }
}
