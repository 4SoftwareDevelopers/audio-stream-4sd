package com.forsoftwaredevelopers.audio_stream_api.application.service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.MarkAsBlackListCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ReceiveVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilterStatus;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AccessFilterRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.AccessFilterJPARepository;
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
class MarkAsBlackListServiceIntegrationTest {

    @Autowired
    private MarkAsBlackListService markAsBlackListService;

    @Autowired
    private VoiceMessageService voiceMessageService;

    @Autowired
    private AccessFilterRepository accessFilterRepository;

    @Autowired
    private VoiceMessageJPARepository voiceMessageJPARepository;

    @Autowired
    private VoiceMessageAudioJPARepository voiceMessageAudioJPARepository;

    @Autowired
    private AccessFilterJPARepository accessFilterJPARepository;

    @BeforeEach
    void setUp() {
        voiceMessageAudioJPARepository.deleteAll();
        voiceMessageJPARepository.deleteAll();
        accessFilterJPARepository.deleteAll();
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
    void markAsBlackList_withValidData_createsBlacklistEntry() {
        byte[] audio = createValidWavAudio(5);
        Result<String> messageResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-123", "testuser", "test@example.com", audio));
        String messageId = messageResult.getOrThrow();

        MarkAsBlackListCommand command = new MarkAsBlackListCommand(
                messageId, "192.168.1.1", "testuser", "test@example.com");

        Result<Void> result = markAsBlackListService.markAsBlackList(command);

        assertTrue(result.isOk());
    }

    @Test
    void markAsBlackList_withNonExistentMessageId_returnsFail() {
        MarkAsBlackListCommand command = new MarkAsBlackListCommand(
                "non-existent-id", "192.168.1.1", "testuser", "test@example.com");

        Result<Void> result = markAsBlackListService.markAsBlackList(command);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("VOICE_MESSAGE_NOT_FOUND"));
    }

    @Test
    void markAsBlackList_withIpAtLimit_blocksIp() {
        byte[] audio = createValidWavAudio(5);
        
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-1", "user1", "user1@test.com", audio));
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-2", "user2", "user2@test.com", audio));
        voiceMessageService.recieve(new ReceiveVoiceMessageCommand("stream-3", "user3", "user3@test.com", audio));

        var filters = accessFilterRepository.findByIp("192.168.1.1");
        for (var filter : filters) {
            accessFilterRepository.deleteById(filter.getId());
        }

        for (int i = 1; i <= 3; i++) {
            Result<String> msgResult = voiceMessageService.recieve(
                    new ReceiveVoiceMessageCommand("stream-" + i, "user" + i, "user" + i + "@test.com", audio));
            String messageId = msgResult.getOrThrow();
            
            MarkAsBlackListCommand command = new MarkAsBlackListCommand(
                    messageId, "192.168.1.1", "user" + i, "user" + i + "@test.com");
            markAsBlackListService.markAsBlackList(command);
        }

        Result<String> messageResult = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-4", "user4", "user4@test.com", audio));
        String messageId = messageResult.getOrThrow();

        MarkAsBlackListCommand fourthCommand = new MarkAsBlackListCommand(
                messageId, "192.168.1.1", "user4", "user4@test.com");

        Result<Void> result = markAsBlackListService.markAsBlackList(fourthCommand);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("IP_BLOCKED"));
    }

    @Test
    void markAsBlackList_multipleCallsFromSameIp_createsMultipleBlacklistEntries() {
        byte[] audio = createValidWavAudio(5);
        
        Result<String> msg1 = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-1", "user1", "user1@test.com", audio));
        Result<String> msg2 = voiceMessageService.recieve(
                new ReceiveVoiceMessageCommand("stream-2", "user2", "user2@test.com", audio));

        markAsBlackListService.markAsBlackList(
                new MarkAsBlackListCommand(msg1.getOrThrow(), "192.168.1.1", "user1", "user1@test.com"));
        markAsBlackListService.markAsBlackList(
                new MarkAsBlackListCommand(msg2.getOrThrow(), "192.168.1.1", "user2", "user2@test.com"));

        var filters = accessFilterRepository.findByIp("192.168.1.1");
        
        assertEquals(2, filters.size());
    }
}