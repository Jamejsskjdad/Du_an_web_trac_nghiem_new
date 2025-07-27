package com.thanhtam.backend.service;

import java.util.List;

import com.thanhtam.backend.entity.Profile;

public interface ProfileService {
    Profile createProfile(Profile profile);

    List<Profile> getAllProfiles();
    void updateIcon(Long profileId, String icon);

}
