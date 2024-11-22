package com.example.utils;

import com.neurotec.biometrics.NSubject;

public final class FingerProcessor {

    public static byte[] getFingerTemplate(NSubject subject) {
        return subject.getTemplateBuffer().toByteArray();
    }
}
