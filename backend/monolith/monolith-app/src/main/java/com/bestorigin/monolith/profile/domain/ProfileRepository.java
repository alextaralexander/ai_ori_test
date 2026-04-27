package com.bestorigin.monolith.profile.domain;

import java.util.Optional;

public interface ProfileRepository {

    ProfileSnapshot findOrCreate(String ownerUserId);

    ProfileSnapshot save(ProfileSnapshot profile);

    Optional<ProfileSnapshot> findByOwnerUserId(String ownerUserId);
}
