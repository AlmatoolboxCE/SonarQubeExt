package alm.plugin;

import org.sonar.api.BatchExtension;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannersFactory;

import alm.plugin.rules.CBO;
import alm.plugin.rules.CoesioneMetodi;
import alm.plugin.rules.DIT;
import alm.plugin.rules.RFC;
import alm.plugin.rules.WMC;

public class MyJavaFileScannersFactory implements JavaFileScannersFactory, BatchExtension {
	
	private final CheckFactory checkFactory;
	
	public MyJavaFileScannersFactory(CheckFactory checkFactory) {
		this.checkFactory = checkFactory;
	}
	
	@Override
	public Iterable<JavaFileScanner> createJavaFileScanners() {
		// we could simply return Arrays.asList(new ForbiddenAnnotationCheck(), new MethodCallCheck())
		// but it would not set the property values declared in Quality profiles (see
		// field ForbiddenAnnotationCheck#name)
		Checks<JavaFileScanner> checks = checkFactory.create(ALMExtensionDefinition.REPOSITORY_KEY);
		checks.addAnnotatedChecks(checkClasses());
		return checks.all();
	}
	
	/**
	* Lists all the checks provided by the plugin
	*/
	@SuppressWarnings("rawtypes")
	public static Class[] checkClasses() {
		return new Class[] {CoesioneMetodi.class, 
							CBO.class, 
							DIT.class, 
							WMC.class,
							RFC.class};
	}

}
