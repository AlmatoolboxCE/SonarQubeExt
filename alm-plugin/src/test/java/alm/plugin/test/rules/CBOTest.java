package alm.plugin.test.rules;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import alm.plugin.rules.CBO;

public class CBOTest {
	
	@Rule
	public CheckMessagesVerifierRule checkMessagesVerifierRule = new CheckMessagesVerifierRule();
	
	@Test
	public void detected(){
		System.setProperty("numClassiDaAnalizzare", "15");
		SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/java/files/Coupling.java"), new VisitorsBridge(new CBO()));		
		checkMessagesVerifierRule.verify(file.getCheckMessages()).next();
	}

}
