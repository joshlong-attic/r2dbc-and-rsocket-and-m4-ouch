package com.example.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	RSocketRequester requester(RSocketRequester.Builder builder) {
		return builder.connectTcp("localhost", 7777).block();
	}
}

@Log4j2
@Component
@RequiredArgsConstructor
class Runner {

	private final RSocketRequester requester;

	@EventListener(ApplicationReadyEvent.class)
	public void listen() {
		this.requester
			.route("hi")
			.data(Mono.empty())
			.retrieveFlux(Greeting.class)
			.subscribe(log::info);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}
