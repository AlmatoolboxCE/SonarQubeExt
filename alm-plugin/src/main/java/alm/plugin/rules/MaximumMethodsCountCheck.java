package alm.plugin.rules;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;

import alm.plugin.ALMExtensionDefinition;

@Rule(key = MaximumMethodsCountCheck.KEY, 
		name = "Numero massimo di metodi consentito", 
		description = "Numero massimo di metodi consentiti in una classe", 
		tags = {"alm-sice"})
public class MaximumMethodsCountCheck extends BaseTreeVisitor implements JavaFileScanner {
	
	private JavaFileScannerContext context;
	
	public static final String KEY = "MaximumMethodsCount";
	private static final Integer DEFAULT_VALUE = 3;
	private final RuleKey RULE_KEY = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, KEY);
	
	@RuleProperty(defaultValue = "10", description = "Numero massimo di metodi consentiti in una classe")
	Integer maxMethodsAllowed = DEFAULT_VALUE;

	@Override
	public void scanFile(JavaFileScannerContext arg0) {
		this.context = arg0;
		scan(context.getTree());
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		int contatoreMetodi = 0;
		for (Tree treeMember : tree.members()) {
			if(treeMember.is(Tree.Kind.METHOD))
				contatoreMetodi++;
		}
		if(contatoreMetodi > maxMethodsAllowed)
			context.addIssue(tree, RULE_KEY, String.format("Troppi metodi! Nella classe ci sono %n metodi. Il massimo è %n", contatoreMetodi, maxMethodsAllowed));
		super.visitClass(tree);
	}
	
	

}
