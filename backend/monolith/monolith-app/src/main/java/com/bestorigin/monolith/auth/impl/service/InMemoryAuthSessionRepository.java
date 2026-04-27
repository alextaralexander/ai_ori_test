package com.bestorigin.monolith.auth.impl.service;

import com.bestorigin.monolith.auth.domain.AuthSessionRepository;
import com.bestorigin.monolith.auth.domain.AuthSessionSnapshot;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAuthSessionRepository implements AuthSessionRepository {

    private final Map<String, AuthSessionSnapshot> sessions = new ConcurrentHashMap<>();

    @Override
    public AuthSessionSnapshot save(AuthSessionSnapshot snapshot) {
        sessions.put(snapshot.token(), snapshot);
        return snapshot;
    }

    @Override
    public Optional<AuthSessionSnapshot> findByToken(String token) {
        return Optional.ofNullable(sessions.get(token));
    }
}
