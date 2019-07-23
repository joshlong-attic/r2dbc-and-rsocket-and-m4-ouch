package com.example.r2dbc;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class R2dbcApplication {

	@Bean
	ConnectionFactory connectionFactory(@Value("${spring.r2dbc.url}") String url) {
		return ConnectionFactories.get(url);
	}

	public static void main(String[] args) {
		SpringApplication.run(R2dbcApplication.class, args);
	}


}

@Controller
class RSocketGreetings {

	@MessageMapping ("hi")
	String hi (){
		return "hello world!";
	}
}

@Configuration
class HttpGreetings {

	@Bean
	RouterFunction<ServerResponse> routes() {
		return route()
			.GET("/hello", r -> ok().body(Flux.just("EEEK!!!"), String.class))
			.build();
	}
}

@Component
@RequiredArgsConstructor
class Initializer {

	private final ReservationRepository repository;

	@EventListener(ApplicationReadyEvent.class)
	public void ready() throws Exception {

		this.repository
			.deleteAll()
			.thenMany(Flux
				.just("A", "B", "C", "D")
				.flatMap(n -> this.repository.save(new Reservation(null, n))))
			.thenMany(this.repository.findAll())
			.subscribe();
	}
}


interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {
	@Id
	private Integer id;
	private String name;
}