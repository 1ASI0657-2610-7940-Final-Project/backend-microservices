package com.gigu.marketplace.infrastructure.storage;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageAdapterTest {
    @Mock SupabaseStorageClient storageClient;
    SupabaseStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SupabaseStorageAdapter("http://localhost:8080", "gig-media", storageClient);
    }

    @Test
    void storeDelegatesWithNormalizedContentTypeAndPublicUrl() {
        byte[] bytes = new byte[]{1, 2, 3};
        var stored = adapter.store("service-123", "IMAGE/PNG", "banner.png", bytes);

        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> objectPathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(storageClient).upload(bucketCaptor.capture(), objectPathCaptor.capture(), contentTypeCaptor.capture(), bytesCaptor.capture());

        assertEquals("gig-media", bucketCaptor.getValue());
        assertArrayEquals(bytes, bytesCaptor.getValue());
        assertEquals("image/png", contentTypeCaptor.getValue());
        assertTrue(objectPathCaptor.getValue().matches("^services/service-123/[A-Za-z0-9-]+-banner\\.png$"));
        assertEquals("http://localhost:8080/storage/v1/object/public/gig-media/" + objectPathCaptor.getValue(), stored.publicUrl());
    }

    @Test
    void validateObjectPathRejectsInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("/services/x/y.png"));
        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("services\\x\\y.png"));
        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("services/x y.png"));
    }

    @Test
    void buildObjectPathKeepsServicePrefix() {
        String path = adapter.buildObjectPath(UUID.randomUUID().toString(), "file.png");
        assertTrue(path.startsWith("services/"));
        assertTrue(path.endsWith("-file.png"));
    }
}
