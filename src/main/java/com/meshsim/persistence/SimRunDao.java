package com.meshsim.persistence;

import com.meshsim.exception.PersistenceException;

import java.util.List;

public interface SimRunDao {
    void save(SimRunRecord run) throws PersistenceException;

    List<SimRunRecord> findByScenario(String scenarioId) throws PersistenceException;

    record SimRunRecord(String scenarioId, String protocol, int ticks, double pdr,
                         double avgHops, double energyUsed, int lifetimeTicks) {
    }
}
