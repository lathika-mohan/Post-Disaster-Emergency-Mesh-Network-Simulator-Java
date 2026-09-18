package com.meshsim.persistence;

import com.meshsim.exception.PersistenceException;
import com.meshsim.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioDao {
    void save(Scenario scenario) throws PersistenceException;

    Optional<Scenario> findById(String id) throws PersistenceException;

    List<Scenario> findAll() throws PersistenceException;

    void delete(String id) throws PersistenceException;
}
