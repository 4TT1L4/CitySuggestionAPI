package ch.bujaki.suggestion.city.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import ch.bujaki.suggestion.city.model.City;

/**
 * Reads the cities from the "cities.tsv" file.
 */
@Repository
public class CityReader implements AutoCloseable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private BufferedReader input;

	@Value("classpath:cities.tsv")
	public Resource resourceFile;
	
	@PostConstruct
	public void init() throws IOException {
		FileReader fileReader = new FileReader(resourceFile.getFile(), StandardCharsets.UTF_8);
		input = new BufferedReader(fileReader);
	}

	@PreDestroy
	public void cleanUp() {
		try {
			input.close();
		} catch (IOException ex) {
			logger.error("cleanUp - Failed to clean up.", ex);
		}
	}
	
	/**
	 * @return the {@link Stream} of the cities available in the TSV file.
	 */
	public Stream<City> cities() {
		return input.lines()
			.filter( line -> !line.isBlank() )
			.map( line -> line.split("\\t") )
			.filter( cols -> cols.length > 14 )
			.map( this::parseLine )
			.filter( Objects::nonNull ); 
	}

	private City parseLine(String[] cols) {
		try {
			String name = trim(cols[1], "name");
			String region = trim(cols[10], "region");
			String asciiName = trim(cols[2], "asciiName");
			String countryCode = trim(cols[8], "countryCode");
			String populationString = trim(cols[14], "population");
			long population = Long.parseLong(populationString);
			
			return new City(name, region, asciiName, countryCode, population);
		} 
		catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex ) {
			logger.error("parseLine - failed to parse line", ex);
			return null;
		}
	}
	
	private String trim(String value, String name) {
		Objects.requireNonNull(value, () -> name + " must not be null.");
		
		return value.trim();
	}
	
	@Override
	public void close() throws Exception {
		input.close();
	}
}
