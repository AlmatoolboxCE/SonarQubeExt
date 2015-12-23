package alm.plugin.test.rules;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import alm.plugin.rules.MaximumMethodsCountCheck;



public class MaximumMethodsCountCheckTest {
	
	@Rule
	public CheckMessagesVerifierRule checkMessagesVerifierRule = new CheckMessagesVerifierRule();
	
	@Test
	public void detected(){
		
		// Parse a known file and use an instance of the check under test to raise the issue.
		SourceFile file = JavaAstScanner
				.scanSingleFile(new File("src/test/java/files/NumeroMetodi.java"), new VisitorsBridge(new MaximumMethodsCountCheck()));
		
		// Check the message raised by the check
		
		checkMessagesVerifierRule.verify(file.getCheckMessages()).next().atLine(3);
	}

}
