package org.itmo.work.fileservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.itmo.work.fileservice.dto.request.UploadRequest;
import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.itmo.work.fileservice.service.ResumeFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeFileService resumeFileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnPresignedDownloadUrl() throws Exception {
        UUID fileId = UUID.randomUUID();

        when(resumeFileService.getDownloadUrl(fileId))
                .thenReturn("http://minio/presigned-url");

        mockMvc.perform(get("/api/files/resume/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("http://minio/presigned-url"));

        verify(resumeFileService).getDownloadUrl(fileId);
    }

    @Test
    void shouldUploadResume_withoutReplacedField() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UploadRequest req = new UploadRequest(applicationId, userId);

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf-bytes".getBytes()
        );

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req)
        );

        UUID savedFileId = UUID.randomUUID();
        when(resumeFileService.uploadResume(any(), isNull(), any(UploadRequest.class)))
                .thenReturn(new UploadResumeResponse(savedFileId, null));

        mockMvc.perform(
                        multipart("/api/files/resume")
                                .file(filePart)
                                .file(dataPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.field_id").value(savedFileId.toString()))
                .andExpect(jsonPath("$.replaced_field").isEmpty()); // или .isNull()

        verify(resumeFileService).uploadResume(any(), isNull(), eq(req));
    }

    @Test
    void shouldUploadResume_withReplacedField() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID replacedField = UUID.randomUUID();

        UploadRequest req = new UploadRequest(applicationId, userId);

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf-bytes".getBytes()
        );

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req)
        );

        UUID savedFileId = UUID.randomUUID();
        when(resumeFileService.uploadResume(any(), eq(replacedField), any(UploadRequest.class)))
                .thenReturn(new UploadResumeResponse(savedFileId, replacedField));

        mockMvc.perform(
                        multipart("/api/files/resume")
                                .file(filePart)
                                .file(dataPart)
                                .param("replacedField", replacedField.toString())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.field_id").value(savedFileId.toString()))
                .andExpect(jsonPath("$.replaced_field").value(replacedField.toString()));

        verify(resumeFileService).uploadResume(any(), eq(replacedField), eq(req));
    }
}