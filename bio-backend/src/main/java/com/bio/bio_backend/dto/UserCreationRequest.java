package com.bio.bio_backend.dto;

import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Role;

import java.util.List;
import java.util.Map;

public record UserCreationRequest(String firstName,
                                  String lastName,
                                  String pesel,
                                  Role role,
                                  Map<FingerType, byte[]> fingerprintImageData,
                                  List<Long> roomIds) {
}
