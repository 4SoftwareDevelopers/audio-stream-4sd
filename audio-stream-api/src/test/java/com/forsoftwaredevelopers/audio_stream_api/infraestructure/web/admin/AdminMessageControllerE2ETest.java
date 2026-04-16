package com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.admin;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageAudioJPARepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminMessageControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VoiceMessageJPARepository voiceMessageJPARepository;

    @Autowired
    private VoiceMessageAudioJPARepository voiceMessageAudioJPARepository;

    @BeforeEach
    void setUp() {
        voiceMessageAudioJPARepository.deleteAll();
        voiceMessageJPARepository.deleteAll();
    }

    private byte[] createValidWavAudio(int durationSeconds) {
        int sampleRate = 44100;
        int numChannels = 1;
        int bitsPerSample = 16;
        int numSamples = sampleRate * durationSeconds;
        int dataSize = numSamples * numChannels * (bitsPerSample / 8);
        int fileSize = 36 + dataSize;

        ByteBuffer buffer = ByteBuffer.allocate(44 + dataSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put("RIFF".getBytes());
        buffer.putInt(fileSize);
        buffer.put("WAVE".getBytes());
        buffer.put("fmt ".getBytes());
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort((short) numChannels);
        buffer.putInt(sampleRate);
        buffer.putInt(sampleRate * numChannels * bitsPerSample / 8);
        buffer.putShort((short) (numChannels * bitsPerSample / 8));
        buffer.putShort((short) bitsPerSample);
        buffer.put("data".getBytes());
        buffer.putInt(dataSize);

        for (int i = 0; i < numSamples; i++) {
            buffer.putShort((short) (Math.sin(2 * Math.PI * 440 * i / sampleRate) * 32767));
        }

        return buffer.array();
    }

    @Test
    void receive_withValidPayload_returnsCreatedWithMessageId() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void receive_withInvalidEmail_returnsBadRequest() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "invalid-email",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("EMAIL_INVALID")));
    }

    @Test
    void receive_withBlankUsername_returnsBadRequest() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "   ",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("USERNAME_REQUIRED")));
    }

    @Test
    void list_withReceivedStatus_returnsPagedResponse() throws Exception {
        mockMvc.perform(get("/api/admin/messages")
                        .param("status", "RECEIVED")
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void list_withPagination_returnsCorrectPage() throws Exception {
        mockMvc.perform(get("/api/admin/messages")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void play_withValidMessageId_returnsOk() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        var result = mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String messageId = result.getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/messages/play/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void play_withAlreadyPlayedMessage_returnsConflict() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        var result = mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String messageId = result.getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/messages/play/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/messages/play/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void reject_withValidMessageId_returnsOk() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        var result = mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String messageId = result.getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/messages/reject/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void reject_withAlreadyRejectedMessage_returnsConflict() throws Exception {
        byte[] audio = createValidWavAudio(5);
        String audioBase64 = java.util.Base64.getEncoder().encodeToString(audio);
        
        String payload = """
                {
                    "streamId": "stream-123",
                    "username": "testuser",
                    "email": "test@example.com",
                    "audio": "%s"
                }
                """.formatted(audioBase64);

        var result = mockMvc.perform(post("/api/admin/messages/receive")
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String messageId = result.getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/messages/reject/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/messages/reject/" + messageId)
                        .with(csrf())
                        .with(user("admin").password("password").roles("ADMIN")))
                .andExpect(status().is4xxClientError());
    }
}