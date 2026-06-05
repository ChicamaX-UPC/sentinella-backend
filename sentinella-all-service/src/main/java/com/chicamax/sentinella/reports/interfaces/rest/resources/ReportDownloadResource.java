package com.chicamax.sentinella.reports.interfaces.rest.resources;

import java.net.URI;
import java.util.UUID;

public record ReportDownloadResource(UUID reportId, URI downloadUrl) {
}
