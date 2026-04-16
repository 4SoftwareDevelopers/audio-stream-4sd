package com.forsoftwaredevelopers.audio_stream_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AudioStreamApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AudioStreamApiApplication.class, args);
	}

}
