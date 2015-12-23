package alm.plugin.rules;

import java.util.regex.Pattern;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.declaration.MethodTreeImpl;
import org.sonar.java.model.declaration.VariableTreeImpl;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;

import alm.plugin.ALMExtensionDefinition;

import com.sonar.sslr.api.AstNode;

@Rule(key = MaximumMethodsCountCheck.KEY, 
		name = "Violazione dell'incapsulamento di una classe", 
		description = "Numero dei metodi che accedono a  dati definiti in un’altra Classe ", 
		tags = {"alm-sice"})
public class PubData extends BaseTreeVisitor implements JavaFileScanner{
	
	private JavaFileScannerContext context;
	public static final String KEY = "PubData";
	private static final Integer DEFAULT_VALUE = 95;
	private final RuleKey RULE_KEY = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, KEY);
	
	private Pattern pattern = null;
	
	@RuleProperty(defaultValue = "95", description = "Percentuale minima delle classi che devono rispettare il paradigma dell'incapsulamento")
	Integer classRespectIncapsulating = DEFAULT_VALUE;

	@Override
	public void scanFile(JavaFileScannerContext arg0) {
		
		
		
		this.context = arg0;
		scan(context.getTree());		
	}
	
	@Override
	public void visitMethod(MethodTree tree) {
		System.out.println("Il parent del metodo "+tree.simpleName().name()+" è "+((MethodTreeImpl)tree).getParent());
		AstNode parentMetodo = ((MethodTreeImpl)tree).getParent();
		for (StatementTree statement : tree.block().body()) {
			System.out.println("------>"+statement.toString());
			
			if(statement.is(Kind.VARIABLE)){
				System.out.println("La variabile è: "+((VariableTree)statement).type());
				System.out.println("La variabile è: "+((VariableTreeImpl)statement).getSymbol().owner().getName());
			}
			
		}
		super.visitMethod(tree);
	}	

}
