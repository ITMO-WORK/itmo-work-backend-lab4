package org.itmo.work.fileservice.adapter.input.web;

import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.application.port.output.*;
import org.itmo.work.fileservice.config.JwtService;
import org.itmo.work.fileservice.domain.model.EntityType;
import org.itmo.work.fileservice.domain.model.FilePurpose;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinioControllerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");
    @Container
    static final MinIOContainer MINIO =
            new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
                    .withUserName("minioadmin")
                    .withPassword("minioadmin123");

    static {
        POSTGRES.start();
    }

    static {
        MINIO.start();
    }

    @Autowired
    MockMvc mockMvc;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.itmo.work.fileservice.application.port.output.FileEventPublisherPort fileEventPublisherPort;
    @Autowired
    ApplicationRegistryPort applicationRegistryPort;
    @Autowired
    FileRepositoryPort fileRepositoryPort;
    @Autowired
    FileStoragePropsPort fileStoragePropsPort;
    @Autowired
    JwtService jwtService;
    @Autowired
    CurrentUserPort currentUserPort;
    @MockitoBean
    FileStoragePort fileStoragePort;
    private UUID userId;
    private String bearer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        r.add("spring.liquibase.user", POSTGRES::getUsername);
        r.add("spring.liquibase.password", POSTGRES::getPassword);

        r.add("minio.internal-endpoint", MINIO::getS3URL);
        r.add("minio.public-endpoint", MINIO::getS3URL);

        r.add("minio.access-key", () -> "minioadmin");
        r.add("minio.secret-key", () -> "minioadmin123");
        r.add("minio.bucket", () -> "files");
        r.add("minio.presign-expiry", () -> "10m");

        r.add("spring.cloud.discovery.enabled", () -> "false");
        r.add("eureka.client.enabled", () -> "false");
    }

    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);

        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        bearer = "Bearer " + generateJwt(userId, "user@mail.com", "ROLE_USER");
    }


    @Test
    void uploadResume_success_200_persists_and_publishes_event() throws Exception {
        UUID applicationId = UUID.randomUUID();

        applicationRegistryPort.register(applicationId, userId, OffsetDateTime.now());

        doNothing().when(fileStoragePort).put(anyString(), anyString(), any());

        doNothing().when(fileEventPublisherPort).publishFileUploadedEvent(any(FileUploadEvent.class));

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "hello".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/file/resume")
                                .file(file)
                                .param("applicationId", applicationId.toString())
                                .header("Authorization", bearer)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.file_id", not(emptyOrNullString())));


        var storedOpt = fileRepositoryPort.getResumeByApplicationId(applicationId);
        assertTrue(storedOpt.isPresent(), "Resume should be persisted in DB");

        var stored = storedOpt.get();
        assertEquals(applicationId, stored.getEntityId());
        assertEquals(EntityType.APPLICATION, stored.getEntityType());
        assertEquals(FilePurpose.APPLICATION_RESUME, stored.getPurpose());
        assertNotNull(stored.getOwnerId());

        verify(fileStoragePort).put(eq(fileStoragePropsPort.bucket()), startsWith("resume/"), any());
        verify(fileEventPublisherPort).publishFileUploadedEvent(any(FileUploadEvent.class));
    }

    @Test
    void uploadResume_applicationNotFound_404() throws Exception {
        UUID applicationId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "hello".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/file/resume")
                                .file(file)
                                .param("applicationId", applicationId.toString())
                                .header("Authorization", bearer)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isNotFound());

        verifyNoInteractions(fileStoragePort, fileEventPublisherPort);
    }

    @Test
    void uploadResume_resumeAlreadyExists_409() throws Exception {
        UUID applicationId = UUID.randomUUID();

        applicationRegistryPort.register(applicationId, userId, OffsetDateTime.now());


        fileRepositoryPort.save(
                StoredFile.builder()
                        .bucket(fileStoragePropsPort.bucket())
                        .entityId(applicationId)
                        .entityType(EntityType.APPLICATION)
                        .ownerId(userId)
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey("resume/existing-key")
                        .originalFileName("cv.pdf")
                        .contentType("application/pdf")
                        .sizeBytes(1L)
                        .createdAt(Instant.now())
                        .build()
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv2.pdf", "application/pdf", "hello".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/file/resume")
                                .file(file)
                                .param("applicationId", applicationId.toString())
                                .header("Authorization", bearer)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isConflict());

        verifyNoInteractions(fileStoragePort, fileEventPublisherPort);
    }


    @Test
    void getDownloadUrl_success_200() throws Exception {
        UUID applicationId = UUID.randomUUID();
        applicationRegistryPort.register(applicationId, userId, OffsetDateTime.now());

        StoredFile saved = fileRepositoryPort.save(
                StoredFile.builder()
                        .bucket(fileStoragePropsPort.bucket())
                        .entityId(applicationId)
                        .entityType(EntityType.APPLICATION)
                        .ownerId(userId)
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey("resume/key-1")
                        .originalFileName("cv.pdf")
                        .contentType("application/pdf")
                        .sizeBytes(10L)
                        .createdAt(Instant.now())
                        .build()
        );

        when(fileStoragePort.getPresignedGetUrl(
                eq(saved.getBucket()),
                eq(saved.getObjectKey()),
                any()
        )).thenReturn("https://example.com/presigned");

        mockMvc.perform(
                        get("/api/file/resume/{applicationId}", applicationId)
                                .header("Authorization", bearer)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("https://example.com/presigned"));

        verify(fileStoragePort).getPresignedGetUrl(eq(saved.getBucket()), eq(saved.getObjectKey()), any());
    }

    @Test
    void getDownloadUrl_resumeNotFound_404() throws Exception {
        UUID applicationId = UUID.randomUUID();
        applicationRegistryPort.register(applicationId, userId, OffsetDateTime.now());

        mockMvc.perform(
                        get("/api/file/resume/{applicationId}", applicationId)
                                .header("Authorization", bearer)
                )
                .andExpect(status().isNotFound());

        verifyNoInteractions(fileStoragePort);
    }


    @Test
    void updateResume_success_200_updates_db() throws Exception {
        UUID applicationId = UUID.randomUUID();
        applicationRegistryPort.register(applicationId, userId, OffsetDateTime.now());

        StoredFile stored = fileRepositoryPort.save(
                StoredFile.builder()
                        .bucket(fileStoragePropsPort.bucket())
                        .entityId(applicationId)
                        .entityType(EntityType.APPLICATION)
                        .ownerId(userId)
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey("resume/key-to-update")
                        .originalFileName("old.pdf")
                        .contentType("application/pdf")
                        .sizeBytes(1L)
                        .createdAt(Instant.now())
                        .build()
        );

        assertNotNull(stored.getId(), "DB must generate file id");

        doNothing().when(fileStoragePort).put(anyString(), anyString(), any());

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.pdf", "application/pdf", "NEW".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/file/resume")
                                .file(file)
                                .param("applicationId", applicationId.toString())
                                .with(req -> {
                                    req.setMethod("PATCH");
                                    return req;
                                })
                                .header("Authorization", bearer)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.file_id").value(stored.getId().toString()))
                .andExpect(jsonPath("$.file_name").value("new.pdf"));

        var updatedOpt = fileRepositoryPort.getResumeByApplicationId(applicationId);
        assertTrue(updatedOpt.isPresent());

        var updated = updatedOpt.get();
        assertEquals(stored.getId(), updated.getId());
        assertEquals("new.pdf", updated.getOriginalFileName());
        assertEquals("application/pdf", updated.getContentType());
        assertEquals(3L, updated.getSizeBytes());

        verify(fileStoragePort).put(eq(fileStoragePropsPort.bucket()), eq(stored.getObjectKey()), any());
    }

    @Test
    void updateResume_applicationNotFound_404() throws Exception {
        UUID applicationId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.pdf", "application/pdf", "NEW".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/file/resume")
                                .file(file)
                                .param("applicationId", applicationId.toString())
                                .with(req -> {
                                    req.setMethod("PATCH");
                                    return req;
                                })
                                .header("Authorization", bearer)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isNotFound());

        verifyNoInteractions(fileStoragePort, fileEventPublisherPort);
    }
}