package com.AverageCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayDeque;
import java.util.Deque;

@SpringBootApplication
public class AverageCalcApplication {

	public static void main(String[] args) {
		SpringApplication.run(AverageCalcApplication.class, args);
	}

}

@RestController
class AverageCalculatorController {

	private final RestTemplate restTemplate = new RestTemplate();
	private final Deque<Integer> window = new ArrayDeque<>();
	private static final int WINDOW_SIZE = 10;
	private static final String TEST_SERVER_BASE_URL = "http://20.244.56.144/test/";

	@GetMapping("/numbers/{qualifier}")
	public ResponseEntity<AverageResponse> calculateAverage(@PathVariable String qualifier) {
		long startTime = System.currentTimeMillis();

		String url = TEST_SERVER_BASE_URL + getEndpointForQualifier(qualifier);
		Integer[] numbers = restTemplate.getForObject(url, Integer[].class);

		if (numbers == null || numbers.length == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new AverageResponse("Failed to fetch numbers from the server", null, null, 0));
		}

		int[] prevWindowState = window.stream().mapToInt(Integer::intValue).toArray();
		for (Integer number : numbers) {
			if (window.size() >= WINDOW_SIZE) {
				window.pollFirst(); // Remove oldest number if window size exceeded
			}
			window.offerLast(number);
		}
		int[] windowCurrState = window.stream().mapToInt(Integer::intValue).toArray();
		double average = calculateAverage(window);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 500) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new AverageResponse("Response time exceeded 500ms", null, null, 0));
		}

		return ResponseEntity.ok(new AverageResponse(null, numbers, windowCurrState, average));
	}

	private String getEndpointForQualifier(String qualifier) {
		switch (qualifier) {
			case "p":
				return "primes";
			case "f":
				return "fibo";
			case "e":
				return "even";
			case "r":
				return "rand";
			default:
				throw new IllegalArgumentException("Invalid qualifier: " + qualifier);
		}
	}

	private double calculateAverage(Deque<Integer> window) {
		if (window.isEmpty()) {
			return 0;
		}
		int sum = window.stream().mapToInt(Integer::intValue).sum();
		return (double) sum / window.size();
	}
}

class AverageResponse {
	private String error;
	private Integer[] numbers;
	private int[] windowCurrState;
	private double avg;

	public AverageResponse(String error, Integer[] numbers, int[] windowCurrState, double avg) {
		this.error = error;
		this.numbers = numbers;
		this.windowCurrState = windowCurrState;
		this.avg = avg;
	}

	// Getters and setters

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Integer[] getNumbers() {
		return numbers;
	}

	public void setNumbers(Integer[] numbers) {
		this.numbers = numbers;
	}

	public int[] getWindowCurrState() {
		return windowCurrState;
	}

	public void setWindowCurrState(int[] windowCurrState) {
		this.windowCurrState = windowCurrState;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}
}