package com.forsoftwaredevelopers.audio_stream_api.infraestructure.audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.stereotype.Component;

import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioValidationPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioValidationPort.AudioValidationResult;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Component
public class JavaSoundAudioValidationAdapter implements AudioValidationPort {

    public static final String AUDIO_FORMAT_UNSUPPORTED = "AUDIO_FORMAT_UNSUPPORTED";
    public static final String AUDIO_INVALID = "AUDIO_INVALID";
    public static final String AUDIO_TOO_LONG = "AUDIO_TOO_LONG";
    
    public static final int MAX_DURATION_SECONDS = 90;
    
    private static final String SUPPORTED_FORMAT_WAV = "WAV";
    private static final String SUPPORTED_FORMAT_MP3 = "MP3";

    @Override
    public Result<AudioValidationResult> validate(byte[] audioData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bais)) {
            
            AudioFormat format = audioInputStream.getFormat();
            String formatName = getFormatName(format);
            
            if (!isSupportedFormat(formatName)) {
                return Result.fail(new DomainError(
                    AUDIO_FORMAT_UNSUPPORTED,
                    "Audio format not supported. Only WAV and MP3 are allowed",
                    ErrorType.VALIDATION
                ));
            }
            
            long frameLength = audioInputStream.getFrameLength();
            float sampleRate = format.getSampleRate();
            int durationSeconds = (int) (frameLength / sampleRate);
            
            if (durationSeconds > MAX_DURATION_SECONDS) {
                return Result.fail(new DomainError(
                    AUDIO_TOO_LONG,
                    "Audio duration exceeds " + MAX_DURATION_SECONDS + " seconds",
                    ErrorType.VALIDATION
                ));
            }
            
            AudioValidationResult result = new AudioValidationResult(
                durationSeconds,
                audioData.length,
                formatName
            );
            
            return Result.ok(result);
            
        } catch (UnsupportedAudioFileException e) {
            return Result.fail(new DomainError(
                AUDIO_INVALID,
                "Invalid audio file: " + e.getMessage(),
                ErrorType.VALIDATION
            ));
        } catch (IOException e) {
            return Result.fail(new DomainError(
                AUDIO_INVALID,
                "Error reading audio data: " + e.getMessage(),
                ErrorType.INTERNAL
            ));
        }
    }
    
    private String getFormatName(AudioFormat format) {
        String encoding = format.getEncoding().toString().toUpperCase();
        if (encoding.contains("MP3")) {
            return SUPPORTED_FORMAT_MP3;
        }
        return SUPPORTED_FORMAT_WAV;
    }
    
    private boolean isSupportedFormat(String formatName) {
        return SUPPORTED_FORMAT_WAV.equals(formatName) || SUPPORTED_FORMAT_MP3.equals(formatName);
    }
}