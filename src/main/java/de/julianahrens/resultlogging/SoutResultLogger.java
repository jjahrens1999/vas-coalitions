package de.julianahrens.resultlogging;

public class SoutResultLogger implements ResultLogger{

    private int counter = 1;

    @Override
    public void log(SimulationResult result) {
        System.out.println("Simulation number: " + counter++);
        System.out.println("Data center actual costs: " + result.getActualCosts());
        System.out.println("Data center profits: " + result.getProfits());
        System.out.println("Coalition configuration: " + result.getCoalitionConfiguration());
        System.out.println("Delegations: " + result.getDelegations());
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}
