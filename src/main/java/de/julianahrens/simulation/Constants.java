package de.julianahrens.simulation;

import de.julianahrens.resultlogging.DuckDbResultLogger;
import de.julianahrens.resultlogging.ResultLogger;

import java.util.List;

public class Constants {

    // Static reward that a data center receives for running a job
    public static final double JOB_REWARD = 30.0;

    // Static share of the difference between price of the data center delegating a job and the one running it that the delegating data center receives
    public static final double ALPHA = 0.5;

    // Means of the datacenters to be simulated
    public static final List<Integer> DATA_CENTER_COSTS = List.of(15, 16, 17, 18, 19, 20);

    // Standard deviation of the data center costs
    public static final int STANDARD_DEVIATION = 2;

    // Number of rounds to run the simulation for
    public static final int N_SIMULATION_ROUNDS = 100;

    // Seed for Random object used in Simulation
    public static final long RANDOM_SEED = 42;

    // DuckDB file name
    public static final String DUCKDB_NAME = "run-1-22-08.db";

    // Result logger
    public static final ResultLogger LOGGER = new DuckDbResultLogger();
}
