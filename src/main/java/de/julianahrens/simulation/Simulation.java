package de.julianahrens.simulation;

import de.julianahrens.datacenter.Coalition;
import de.julianahrens.datacenter.ComputeClusterElement;
import de.julianahrens.datacenter.DataCenter;
import de.julianahrens.datacenter.FederatedComputeCluster;
import de.julianahrens.resultlogging.SimulationResult;

import java.util.*;
import java.util.stream.Stream;

public class Simulation {

    private final List<List<Set<Integer>>> coalitionConfigurations;

    {
        // populate coalition configurations
        coalitionConfigurations = generatePossiblePartitions(range(0, Constants.DATA_CENTER_COSTS.size()));
    }

    public List<Integer> range(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(i);
        }
        return result;
    }

    private List<List<Set<Integer>>> generatePossiblePartitions(List<Integer> set) {
        List<List<Set<Integer>>> response = new ArrayList<>();

        if (set.isEmpty()) {
            response.add(new ArrayList<>());
            return response;
        }

        int firstElement = set.getFirst();
        List<Integer> rest = set.subList(1, set.size());

        List<List<Set<Integer>>> possiblePartitionsInRest = generatePossiblePartitions(rest);

        for (List<Set<Integer>> possiblePartition : possiblePartitionsInRest) {
            List<Set<Integer>> newPartition = new ArrayList<>(possiblePartition);
            Set<Integer> newPartitionElement = new HashSet<>();
            newPartitionElement.add(firstElement);
            newPartition.add(newPartitionElement);
            response.add(newPartition);

            for (int i = 0; i < possiblePartition.size(); i++) {
                List<Set<Integer>> modifiedPartition = new ArrayList<>();
                for (int j = 0; j < possiblePartition.size(); j++) {
                    if (i == j) {
                        Set<Integer> newSubset = new HashSet<>(possiblePartition.get(j));
                        newSubset.add(firstElement);
                        modifiedPartition.add(newSubset);
                    } else {
                        modifiedPartition.add(new HashSet<>(possiblePartition.get(j)));
                    }
                }
                response.add(modifiedPartition);
            }
        }
        return response;
    }

    private Stream<Coalition> mapCoalitionFromConfiguration(Set<Integer> configuration, List<DataCenter> dataCenters, FederatedComputeCluster cluster) {
        if (configuration.size() > 1) {
            return Stream.of(new Coalition(configuration.stream().map(dataCenters::get).toList(), cluster));
        }
        return Stream.empty();
    }

    private SimulationResult runSingleSimulation(List<DataCenter> dataCenters, List<Coalition> coalitions, List<Set<Integer>> coalitionConfiguration, double deviationMultiplier) {
        List<ComputeClusterElement> clusterElements = Stream.concat(dataCenters.stream(), coalitions.stream()).toList();
        clusterElements.forEach(ComputeClusterElement::doDelegate);
        clusterElements.forEach(ComputeClusterElement::doConfirmOrRejectDelegationRequests);

        List<Double> profits = dataCenters.stream().map(DataCenter::calculateProfit).toList();
        List<Double> actualCosts = dataCenters.stream().map(DataCenter::getActualCost).toList();

        String delegations = DelegationLog.instance.toString();

        return new SimulationResult(deviationMultiplier, actualCosts, profits, coalitionConfiguration, delegations);
    }

    public void simulate() {
        for (double deviationMultiplier = 0; deviationMultiplier <= 1; deviationMultiplier += 0.1) {
            for (List<Set<Integer>> coalitionConfiguration : coalitionConfigurations) {
                Random rand = new Random(Constants.RANDOM_SEED);
                for (int round = 0; round < Constants.N_SIMULATION_ROUNDS; round++) {
                    List<DataCenter> dataCenters = new ArrayList<>();
                    FederatedComputeCluster cluster = new FederatedComputeCluster(dataCenters);
                    for (int i = 0; i < Constants.DATA_CENTER_COSTS.size(); i++) {
                        double mean = Constants.DATA_CENTER_COSTS.get(i);
                        double actualValue = (rand.nextGaussian() * (Constants.STANDARD_DEVIATION * deviationMultiplier)) + mean;
                        dataCenters.add(new DataCenter(i, cluster, mean, actualValue));
                    }

                    List<Coalition> coalitions = coalitionConfiguration.stream().flatMap(configuration -> mapCoalitionFromConfiguration(configuration, dataCenters, cluster)).toList();

                    SimulationResult result = runSingleSimulation(dataCenters, coalitions, coalitionConfiguration, deviationMultiplier);

                    Constants.LOGGER.log(result);

                    DelegationLog.instance.clear();
                }
            }
        }
    }
}
