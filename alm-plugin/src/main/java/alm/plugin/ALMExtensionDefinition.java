package alm.plugin;

import org.sonar.api.BatchExtension;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;

/**
* Declare rule metadata in server repository of rules. That allows to list the rules
* in the page "Rules".
*/
public class ALMExtensionDefinition implements RulesDefinition, BatchExtension {
	
	 public static final String REPOSITORY_KEY = "my-java-rules";

	@Override
	public void define(Context context) {
		NewRepository repo = context.createRepository(REPOSITORY_KEY, "java");
		repo.setName("Repository di regole Java");
		
		// We could use a XML or JSON file to load all rule metadata, but
		// we prefer use annotations in order to have all information in a single place
		RulesDefinitionAnnotationLoader annotationLoader = new RulesDefinitionAnnotationLoader();
		annotationLoader.load(repo, MyJavaFileScannersFactory.checkClasses());
		repo.done();
	}
	
	

}
