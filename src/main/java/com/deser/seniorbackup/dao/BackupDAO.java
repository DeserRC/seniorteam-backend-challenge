package com.deser.seniorbackup.dao;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.database.DataBase;
import com.deser.seniorbackup.model.BackupArgument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BackupDAO {
    private final SeniorBackup main;
    private final DataBase dataBase;
    private final ObjectMapper mapper;

    public BackupDAO(final SeniorBackup main, final DataBase dataBase) {
        this.main = main;
        this.dataBase = dataBase;
        this.mapper = new ObjectMapper();
    }

    public CompletableFuture<Boolean> isExists(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final PreparedStatement stm = dataBase.getConnection().prepareStatement("SELECT 1 FROM `backup` WHERE `uuid` = ?")) {
                stm.setString(1, uuid);
                final ResultSet rs = stm.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                System.err.printf("An error occurred while checking if the uuid \"%s\" exists in the database: %s", uuid, e.getMessage());
            }
            return false;
        }, main.getExecutor());
    }

    public CompletableFuture<Map<String, BackupArgument>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, BackupArgument> inventories = new HashMap<>();
            try (final PreparedStatement stm = dataBase.getConnection().prepareStatement("SELECT `uuid`, `inventories` FROM `backup`")) {
                final ResultSet rs = stm.executeQuery();
                while (rs.next()) {
                    final String uuid = rs.getString("uuid");
                    final String json = rs.getString("inventories");
                    BackupArgument backup = mapper.readValue(json, BackupArgument.class);
                    inventories.put(uuid, backup);
                }
            } catch (SQLException | JsonProcessingException e) {
                System.err.printf("An error get all players data: %s", e.getMessage());
            }
            return inventories;
        }, main.getExecutor());
    }

    public CompletableFuture<BackupArgument> getArgument(final String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final PreparedStatement stm = dataBase.getConnection().prepareStatement("SELECT `inventories` FROM `backup` WHERE `uuid` = ?")) {
                stm.setString(1, uuid);
                final ResultSet rs = stm.executeQuery();
                if (rs.next()) {
                    final String json = rs.getString("inventories");
                    return mapper.readValue(json, BackupArgument.class);
                }
            } catch (SQLException | JsonProcessingException e) {
                System.err.printf("An error occurred while get data of uuid \"%s\": %s", uuid, e.getMessage());
            }
            return null;
        }, main.getExecutor());
    }

    public <T extends BackupArgument> void replace(final String uuid, final T argument) {
        CompletableFuture.runAsync(() -> {
            try (final PreparedStatement stm = dataBase.getConnection().prepareStatement("REPLACE INTO `backup` (`uuid`, `inventories`) VALUES (?,?)")) {
                stm.setString(1, uuid);
                stm.setString(2, mapper.writeValueAsString(argument));
                stm.executeUpdate();
            } catch (SQLException | JsonProcessingException e) {
                System.err.printf("An error saving da of uuid \"%s\": %s", uuid, e.getMessage());
            }
        }, main.getExecutor());
    }

    public void delete(final String uuid) {
        CompletableFuture.runAsync(() -> {
            try (final PreparedStatement stm = dataBase.getConnection().prepareStatement("DELETE FROM `backup` WHERE `uuid` = ?")) {
                stm.setString(1, uuid);
                stm.executeUpdate();
            } catch (SQLException e) {
                System.err.printf("An error deleting data of uuid \"%s\": %s", uuid, e.getMessage());
            }
        }, main.getExecutor());
    }
}

