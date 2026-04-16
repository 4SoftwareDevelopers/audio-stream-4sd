package com.forsoftwaredevelopers.audio_stream_api.application.service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListPendingVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PlayVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ReceiveVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.RejectVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessageStatus;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.VoiceMessageRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageAudioJPARepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VoiceMessageServiceIntegrationTest {

    @Autowired
    private VoiceMessageService voiceMessageService;

    @Autowired
    private VoiceMessageRepository voiceMessageRepository;

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
    void receive_withValidCommand_createsMessageWithReceivedStatus() {
        byte[] audio = createValidWavAudio(5);
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", audio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isOk());
        String messageId = result.getOrThrow();
        assertNotNull(messageId);

        VoiceMessage savedMessage = voiceMessageRepository.findById(messageId);
        assertNotNull(savedMessage);
        assertEquals("stream-123", savedMessage.getStreamId());
        assertEquals("testuser", savedMessage.getUsername());
        assertEquals("test@example.com", savedMessage.getEmail());
        assertEquals(VoiceMessageStatus.RECEIVED, savedMessage.getStatus());
    }

    @Test
    void receive_withInvalidEmail_returnsFail() {
        byte[] audio = createValidWavAudio(5);
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "invalid-email", audio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
    }

    @Test
    void receive_withBlankUsername_returnsFail() {
        byte[] audio = createValidWavAudio(5);
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "   ", "test@example.com", audio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
    }

    @Test
    void receive_withAudioTooLarge_returnsFail() {
        byte[] audio = createValidWavAudio(300);
        byte[] largeAudio = new byte[1024 * 1024 + 1];
        System.arraycopy(audio, 0, largeAudio, 0, Math.min(audio.length, largeAudio.length));
        
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", largeAudio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("AUDIO_TOO_LARGE"));
    }

    @Test
    void receive_withAudioTooLong_returnsFail() {
        byte[] invalidAudio = "not a valid audio".getBytes();
        
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", invalidAudio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("AUDIO_INVALID"));
    }

    @Test
    void receive_withValidAudio_savesAudioMetadata() {
        byte[] audio = createValidWavAudio(10);
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", audio);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isOk());
        String messageId = result.getOrThrow();

        var audioResult = voiceMessageRepository.findAudioByVoiceMessageId(messageId);
        assertNotNull(audioResult);
        assertNotNull(audioResult.storageKey());
    }

    @Test
    void list_withReceivedStatus_returnsPagedResponse() {
        byte[] audio = createValidWavAudio(5);
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-1", "user1", "user1@test.com", audio));
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-2", "user2", "user2@test.com", audio));

        PagedResponse<VoiceMessage> result = voiceMessageService.list(
                ListPendingVoiceMessageCommand.of("RECEIVED", null, null, null, null, 0, 20));

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
    }

    @Test
    void list_withFilters_returnsFilteredResults() {
        byte[] audio = createValidWavAudio(5);
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-1", "user1", "user1@test.com", audio));
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-2", "user2", "user2@test.com", audio));

        PagedResponse<VoiceMessage> result = voiceMessageService.list(
                ListPendingVoiceMessageCommand.of("RECEIVED", "stream-1", null, null, null, 0, 20));

        assertEquals(1, result.content().size());
        assertEquals("stream-1", result.content().get(0).getStreamId());
    }

    @Test
    void play_withValidMessageId_updatesStatusToPlayed() {
        byte[] audio = createValidWavAudio(5);
        Result<String> createResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = createResult.getOrThrow();

        Result<Void> result = voiceMessageService.play(new PlayVoiceMessageCommand(messageId));

        assertTrue(result.isOk());
        VoiceMessage updatedMessage = voiceMessageRepository.findById(messageId);
        assertEquals(VoiceMessageStatus.PLAYED, updatedMessage.getStatus());
    }

    @Test
    void play_withAlreadyPlayedMessage_returnsFail() {
        byte[] audio = createValidWavAudio(5);
        Result<String> createResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = createResult.getOrThrow();
        voiceMessageService.play(new PlayVoiceMessageCommand(messageId));

        Result<Void> result = voiceMessageService.play(new PlayVoiceMessageCommand(messageId));

        assertTrue(result.isFail());
    }

    @Test
    void reject_withValidMessageId_updatesStatusToRejected() {
        byte[] audio = createValidWavAudio(5);
        Result<String> createResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = createResult.getOrThrow();

        Result<Void> result = voiceMessageService.reject(new RejectVoiceMessageCommand(messageId));

        assertTrue(result.isOk());
        VoiceMessage updatedMessage = voiceMessageRepository.findById(messageId);
        assertEquals(VoiceMessageStatus.REJECTED, updatedMessage.getStatus());
    }

    @Test
    void reject_withAlreadyRejectedMessage_returnsFail() {
        byte[] audio = createValidWavAudio(5);
        Result<String> createResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = createResult.getOrThrow();
        voiceMessageService.reject(new RejectVoiceMessageCommand(messageId));

        Result<Void> result = voiceMessageService.reject(new RejectVoiceMessageCommand(messageId));

        assertTrue(result.isFail());
    }

    @Test
    void delete_withValidMessageId_deletesMessage() {
        byte[] audio = createValidWavAudio(5);
        Result<String> createResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = createResult.getOrThrow();

        Result<Void> result = voiceMessageService.delete(
                new com.forsoftwaredevelopers.audio_stream_api.application.command.DeleteVoiceMessageCommand(messageId));

        assertTrue(result.isOk());
        VoiceMessage deletedMessage = voiceMessageRepository.findById(messageId);
        assertNull(deletedMessage);
    }

    @Test
    void receive_withNullAudio_returnsFail() {
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", null);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("AUDIO_REQUIRED"));
    }

    @Test
    void receive_withEmptyAudio_returnsFail() {
        ReceiveVoiceMessageCommand command = new ReceiveVoiceMessageCommand(
                "stream-123", "testuser", "test@example.com", new byte[0]);

        Result<String> result = voiceMessageService.recieve(command);

        assertTrue(result.isFail());
    }
}