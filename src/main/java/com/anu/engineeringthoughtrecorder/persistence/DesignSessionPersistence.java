package com.anu.engineeringthoughtrecorder.persistence;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;

import java.util.UUID;

public interface DesignSessionPersistence {

    void save(DesignSession session);

    DesignSession load(UUID sessionId);
}
