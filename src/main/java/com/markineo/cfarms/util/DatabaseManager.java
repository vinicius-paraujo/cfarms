package com.markineo.cfarms.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.markineo.cfarms.farms.BlockFarm;

public class DatabaseManager {
    private static BasicDataSource dataSource = new BasicDataSource();

    private static Connection connection;

    public static void configureDataSource(String url, String user, String password) {
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxTotal(50);

        Duration duration = Duration.ofSeconds(5);
        dataSource.setMaxWait(duration);
    }

    public static Connection getConnection() throws SQLException {
        int retries = 5;
        while (retries > 0) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("Erro ao obter conexão: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                retries--;
            }
        }
        throw new SQLException("Falha ao obter conexão após várias tentativas.");
    }

    public static ResultSet executeQueryRs(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeQuery();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            return null;
        }
    }

    public static void executeQuery(Connection connection, String sql, Object... parameters) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateDatabase(List<BlockFarm> blocksToRemove, List<BlockFarm> blocksToAdd) {
        Connection connection = null;
        try {
            Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] §eIniciando atualização das plantações no banco de dados...");

            connection = getConnection();
            connection.setAutoCommit(false); // Iniciar transação

            for (BlockFarm blockFarm : blocksToRemove) {
                removeBlockFromDatabase(connection, blockFarm);
            }

            for (BlockFarm blockFarm : blocksToAdd) {
                addBlockToDatabase(connection, blockFarm);
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] §eOcorreu um erro ao armazenar as plantações no banco de dados.");
                    connection.rollback(); // Rollback em caso de falha
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] §eAs plantações foram atualizadas no banco de dados.");

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void removeBlockFromDatabase(Connection connection, BlockFarm blockFarm) throws SQLException {
        String deleteQuery = "DELETE FROM cFarms WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setString(1, blockFarm.getBlock().getWorld().getName());
            statement.setDouble(2, blockFarm.getLocation().getX());
            statement.setDouble(3, blockFarm.getLocation().getY());
            statement.setDouble(4, blockFarm.getLocation().getZ());

            statement.executeUpdate();
        }
    }

    private static void addBlockToDatabase(Connection connection, BlockFarm blockFarm) throws SQLException {
        String insertQuery = "INSERT INTO cFarms (world, x, y, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, blockFarm.getBlock().getWorld().getName());
            statement.setDouble(2, blockFarm.getLocation().getX());
            statement.setDouble(3, blockFarm.getLocation().getY());
            statement.setDouble(4, blockFarm.getLocation().getZ());

            statement.executeUpdate();
        }
    }

    public static List<BlockFarm> getAllFarms() {
        List<BlockFarm> farms = new ArrayList<>();
        
        try {
            connection = getConnection();

            ResultSet resultSet = executeQueryRs(connection, "SELECT * FROM cFarms");

            while (resultSet.next()) {
                String worldName = resultSet.getString("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");

                Block block = Bukkit.getWorld(worldName).getBlockAt(x, y, z);

                if (block.getType() != Material.AIR) {
                    BlockFarm farm = new BlockFarm(block);
                    farms.add(farm);
                } else {
                    executeQuery(connection, "DELETE FROM cFarms WHERE world = ? AND x = ? AND y = ? AND z = ?", worldName, x, y, z);
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return farms;
    }

    public static void createTable() {
        try {
            connection = getConnection();

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "cFarms", null);

            if (!tables.next()) {
                executeQuery(connection, "CREATE TABLE cFarms (id INT AUTO_INCREMENT PRIMARY KEY, world VARCHAR(255) NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL);");
            }

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
