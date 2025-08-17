package de.julianahrens.resultlogging;

import de.julianahrens.simulation.Constants;
import org.duckdb.DuckDBConnection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DuckDbResultLogger implements ResultLogger {

    private final DuckDBConnection connection;
    private int counter = 1;

    {
        try {
            connection = (DuckDBConnection) DriverManager.getConnection("jdbc:duckdb:" + Constants.DUCKDB_NAME);
            Statement statement = connection.createStatement();
            statement.execute(buildDdlStatement());
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildDdlStatement() {
        String ddlStart = """
                CREATE TABLE simulations (
                    simulation_number INTEGER PRIMARY KEY,
                    deviation_multiplier DOUBLE,
                """;
        String ddlEnd = """
                    coalition_configuration INTEGER[][],
                    delegations STRING
                );
                """;
        StringBuilder ddlBuilder = new StringBuilder();
        ddlBuilder.append(ddlStart);
        for (int i = 1; i <= Constants.DATA_CENTER_COSTS.size(); i++) {
            ddlBuilder.append("price_dc").append(i).append(" DOUBLE,\n");
        }
        for (int i = 1; i <= Constants.DATA_CENTER_COSTS.size(); i++) {
            ddlBuilder.append("profit_dc").append(i).append(" DOUBLE,\n");
        }
        ddlBuilder.append(ddlEnd);
        return ddlBuilder.toString();
    }

    private String toDuckDbInsertString(List<Set<Integer>> coalitionConfiguration) {
        return "[" + coalitionConfiguration.stream()
                .map(set -> "[" + set.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]")
                .collect(Collectors.joining(", ")) + "]";
    }

    private String buildDmlStatement(SimulationResult result) {
        String dmlStart = """
                INSERT INTO simulations VALUES ( ?, ?,
                """;
        String dmlEnd = "?);";
        return dmlStart +
                "?, ".repeat(Constants.DATA_CENTER_COSTS.size() * 2) +
                // prepared statement does not support list
                toDuckDbInsertString(result.getCoalitionConfiguration()) + ", " +
                dmlEnd;
    }

    @Override
    public void log(SimulationResult result) {
        if (counter % 100 == 0) {
            System.out.println("on simulation" + counter);
        }

        try (PreparedStatement statement = connection.prepareStatement(buildDmlStatement(result))) {
            statement.setInt(1, counter++);
            statement.setDouble(2, result.getDeviationMultiplier());
            for (int i = 0; i < Constants.DATA_CENTER_COSTS.size(); i++) {
                statement.setDouble(i + 3, result.getActualCosts().get(i));
            }
            for (int i = 0; i < Constants.DATA_CENTER_COSTS.size(); i++) {
                statement.setDouble(i + Constants.DATA_CENTER_COSTS.size() + 3, result.getProfits().get(i));
            }
            statement.setString(Constants.DATA_CENTER_COSTS.size() * 2 + 3, result.getDelegations());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
