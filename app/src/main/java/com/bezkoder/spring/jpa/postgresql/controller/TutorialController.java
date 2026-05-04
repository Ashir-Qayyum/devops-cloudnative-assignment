package com.bezkoder.spring.jpa.postgresql.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.spring.jpa.postgresql.model.Tutorial;
import com.bezkoder.spring.jpa.postgresql.repository.TutorialRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

	private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);

	private final Counter createCounter;
	private final Counter readCounter;
	private final Counter updateCounter;
	private final Counter deleteCounter;

	@Autowired
	TutorialRepository tutorialRepository;

	@Autowired
	public TutorialController(TutorialRepository tutorialRepository, MeterRegistry meterRegistry) {
		this.tutorialRepository = tutorialRepository;

		this.createCounter = meterRegistry.counter("tutorial_create_total");
		this.readCounter = meterRegistry.counter("tutorial_read_total");
		this.updateCounter = meterRegistry.counter("tutorial_update_total");
		this.deleteCounter = meterRegistry.counter("tutorial_delete_total");
	}

	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			if (title == null)
				tutorialRepository.findAll().forEach(tutorials::add);
			else
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			readCounter.increment();
			logger.info("Getting all tutorials");

			return new ResponseEntity<>(tutorials, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error getting tutorials");
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			readCounter.increment();
			logger.info("Getting tutorial with id: {}", id);
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			logger.error("Tutorial not found with id: {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));

			createCounter.increment();
			logger.info("Creating tutorial");

			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);

		} catch (Exception e) {
			logger.error("Error creating tutorial");
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			_tutorial.setPublished(tutorial.isPublished());

			updateCounter.increment();
			logger.info("Updating tutorial id: {}", id);

			return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);

		} else {
			logger.error("Error updating tutorial id: {}", id);
			
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);

			deleteCounter.increment();
			logger.info("Deleting tutorial id: {}", id);

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (Exception e) {
			logger.error("Error deleting tutorial id: {}", id);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();

			deleteCounter.increment();
			logger.info("Deleting all tutorials");

			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (Exception e) {
			logger.error("Error deleting all tutorials");
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			readCounter.increment();
			return new ResponseEntity<>(tutorials, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error getting published tutorials");
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
