package com.bestorigin.monolith.publiccontent.api;

public record ContentAttachmentResponse(
        String attachmentKey,
        String fileType,
        String titleKey,
        String url,
        Long fileSizeBytes
) {
}
