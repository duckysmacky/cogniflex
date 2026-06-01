package io.github.duckysmacky.cogniflex;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CogniflexBackendApplicationTests {

	@Autowired
	private List<StaticAnalyzer<? extends AnalysisContext>> staticAnalyzers;

	@Test
	void contextLoads() {
	}

	@Test
	void staticAnalyzersAreWiredForTextAndImage() {
		assertTrue(
			staticAnalyzers.stream().anyMatch(analyzer -> analyzer.supports(ContentType.TEXT)),
			"Expected a static analyzer for TEXT content"
		);
		assertTrue(
			staticAnalyzers.stream().anyMatch(analyzer -> analyzer.supports(ContentType.IMAGE)),
			"Expected a static analyzer for IMAGE content"
		);
	}

}
