package alm.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;

public class ALMExtensionPlugin extends SonarPlugin {

	@SuppressWarnings("rawtypes")
	@Override
	public List getExtensions() {
		return Arrays.asList(ALMExtensionDefinition.class, VisitorSensor.class, MyJavaFileScannersFactory.class);
	}
}
