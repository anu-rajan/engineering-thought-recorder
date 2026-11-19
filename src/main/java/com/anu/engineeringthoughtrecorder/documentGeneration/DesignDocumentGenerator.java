package com.anu.engineeringthoughtrecorder.documentGeneration;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;

public interface DesignDocumentGenerator {

    String generate(DesignSession session);

}