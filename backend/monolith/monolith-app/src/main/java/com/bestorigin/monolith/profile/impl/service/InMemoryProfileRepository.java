package com.bestorigin.monolith.profile.impl.service;

import com.bestorigin.monolith.profile.domain.ProfileRepository;
import com.bestorigin.monolith.profile.domain.ProfileSnapshot;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryProfileRepository implements ProfileRepository {

    private final ConcurrentMap<String, ProfileSnapshot> profiles = new ConcurrentHashMap<>();

    @Override
    public ProfileSnapshot findOrCreate(String ownerUserId) {
        return profiles.computeIfAbsent(ownerUserId, id -> new ProfileSnapshot(
                UUID.nameUUIDFromBytes(("profile-" + id).getBytes(StandardCharsets.UTF_8)),
                id,
                "Анна",
                "Покупатель",
                "ru"
        ));
    }

    @Override
    public ProfileSnapshot save(ProfileSnapshot profile) {
        profiles.put(profile.ownerUserId(), profile);
        return profile;
    }

    @Override
    public Optional<ProfileSnapshot> findByOwnerUserId(String ownerUserId) {
        return Optional.ofNullable(profiles.get(ownerUserId));
    }
}
