package alm.plugin.test.rules;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import alm.plugin.rules.DIT;

public class DITTest {
	
	@Rule
	public CheckMessagesVerifierRule checkMessagesVerifierRule = new CheckMessagesVerifierRule();
	
	@Test
	public void test(){
		System.setProperty("numClassiDaAnalizzare", "8");
		SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/java/files/DIT.java"), new VisitorsBridge(new DIT()));
		checkMessagesVerifierRule.verify(file.getCheckMessages()).next();
	}

}
