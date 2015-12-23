package alm.plugin.test.rules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import alm.plugin.rules.CoesioneMetodi;

public class CoesioneMetodiTest {
	
	@Rule
	public CheckMessagesVerifierRule checkMessagesVerifierRule = new CheckMessagesVerifierRule();
	
	@Test
	public void detected(){
		Settings settings = new Settings();
		settings.setProperty("A", 123);
		System.setProperty("numClassiDaAnalizzare", "1");
		SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/java/files/Incapsulamento.java"), new VisitorsBridge(new CoesioneMetodi()));
		checkMessagesVerifierRule.verify(file.getCheckMessages()).next();
		
	}

}
