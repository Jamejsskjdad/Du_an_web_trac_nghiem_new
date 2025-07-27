package com.thanhtam.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.repository.ProfileRepository;

@Service
public class ProfileServiceImpl implements ProfileService {

    private ProfileRepository profileRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile createProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    @Override
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }
    @Override
    public void updateIcon(Long profileId, String icon) {
        Profile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy profile với ID: " + profileId));
        profile.setIcon(icon);
        profileRepository.save(profile);
    }
    

}
