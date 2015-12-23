package alm.plugin.test.rules;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import alm.plugin.rules.PubData;

public class PubDataTest {
	
	@Rule
	public CheckMessagesVerifierRule checkMessagesVerifierRule = new CheckMessagesVerifierRule();
	
	@Test
	public void detected(){
		SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/java/files/Incapsulamento.java"), new VisitorsBridge(new PubData()));
		
		checkMessagesVerifierRule.verify(file.getCheckMessages()).next();
	}
}
