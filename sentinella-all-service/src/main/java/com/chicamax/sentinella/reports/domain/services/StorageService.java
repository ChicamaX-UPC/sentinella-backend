package com.chicamax.sentinella.reports.domain.services;

import java.net.URI;

public interface StorageService {
    String saveReport(byte[] content, String filename);

    URI getDownloadUri(String storageKey);

    byte[] readReport(String storageKey);
}
