package com.forsoftwaredevelopers.audio_stream_api.infraestructure.audio;

import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioValidationPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JavaSoundAudioValidationAdapterTest {

    @Autowired
    private AudioValidationPort audioValidationPort;

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

    private byte[] createInvalidAudio() {
        return "This is not an audio file".getBytes();
    }

    @Test
    void validate_withValidWav_returnsOkWithMetadata() {
        byte[] audio = createValidWavAudio(10);

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isOk());
        AudioValidationPort.AudioValidationResult validationResult = result.getOrThrow();
        assertEquals(10, validationResult.durationSeconds());
        assertEquals("WAV", validationResult.format());
        assertTrue(validationResult.sizeBytes() > 0);
    }

    @Test
    void validate_withShortWav_returnsOk() {
        byte[] audio = createValidWavAudio(1);

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isOk());
        assertEquals(1, result.getOrThrow().durationSeconds());
    }

    @Test
    void validate_withAudioAtMaxDuration_returnsOk() {
        byte[] audio = createValidWavAudio(90);

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isOk());
        assertEquals(90, result.getOrThrow().durationSeconds());
    }

    @Test
    void validate_withAudioExceedingMaxDuration_returnsFail() {
        byte[] audio = createValidWavAudio(120);

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("AUDIO_TOO_LONG"));
    }

    @Test
    void validate_withInvalidAudioFormat_returnsFail() {
        byte[] audio = createInvalidAudio();

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("AUDIO_INVALID"));
    }

    @Test
    void validate_withEmptyAudio_returnsFail() {
        byte[] audio = new byte[0];

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isFail());
    }

    @Test
    void validate_withCorruptedWavHeader_returnsFail() {
        byte[] audio = createValidWavAudio(5);
        audio[0] = 'X';
        audio[1] = 'Y';
        audio[2] = 'Z';
        audio[3] = 'W';

        Result<AudioValidationPort.AudioValidationResult> result = audioValidationPort.validate(audio);

        assertTrue(result.isFail());
    }
}