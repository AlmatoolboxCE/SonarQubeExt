package alm.plugin.rules;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.declaration.MethodTreeImpl;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ModifierTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;

import alm.plugin.ALMExtensionDefinition;
import alm.plugin.utils.AlmPluginUtils;

import com.google.common.collect.ImmutableList;

@Rule(key = RFC.RULE_KEY,
		name = "OORFC – Risposta per classe",
		description = "Misura il numero di metodi che" +
				"possono essere chiamati in risposta ad un messaggio ricevuto da un oggetto della classe" +
				" o mandato da un metodo della classe stessa",
		tags = {"alm-sice"})
public class RFC extends BaseTreeVisitor implements JavaFileScanner{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String RULE_KEY = "Risposta per classe";
	private final RuleKey ruleKey = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, RULE_KEY);
	
	public static final int DEFAULT_MIN = 100;
	public static final int DEFAULT_LIMIT = 120;
	
	private Integer classiDaAnalizzare;
	private Integer classiAnalizzate;
	private Integer rfcMinimo;
	private Integer rfcLimite;
	private Integer OORFC_minimo;
	private Integer OORFC_limite;
	private boolean violazione;
	
	private JavaFileScannerContext context;
	
	private int rfc = 0;
	
	@RuleProperty(key = "min",
			 		description = "Valore di soglia minimo per la risposta che deve avere la classe",
			 		defaultValue = "" + DEFAULT_MIN)
	private Integer min = DEFAULT_MIN;

	@RuleProperty(key = "limit",
			 		description = "Valore di soglia limite per la risposta che deve avere la classe",
			 		defaultValue = "" + DEFAULT_LIMIT)
	private Integer limit = DEFAULT_LIMIT;
	
	public RFC() {
		logger.info("--- Analisi OORFC ---");
		classiAnalizzate = new Integer(0);
		rfcMinimo = new Integer(0);
		rfcLimite = new Integer(0);
		OORFC_minimo = new Integer(0);
		OORFC_limite = new Integer(0);
		violazione = false;
	}
  
	@Override
	public void scanFile(JavaFileScannerContext ctx) {
		classiDaAnalizzare = Integer.parseInt(System.getProperty("numClassiDaAnalizzare"));
		context = ctx;
		scan(context.getTree());		
		
		if (classiAnalizzate.equals(classiDaAnalizzare)) {
			logger.info(String.format("***OORFC*** Classi analizzate: %d", classiAnalizzate));
			OORFC_minimo = AlmPluginUtils.arrotonda((rfcMinimo.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia minimo
			OORFC_limite = AlmPluginUtils.arrotonda((rfcLimite.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia limite
			
			if(violazione){  //se è presente anche una sola classe che non rispetta la soglia limite, si solleva la violazione
				logger.info(String.format("Numero classi che rispettano RFC limite: %d ", rfcLimite));
				logger.info(String.format("Valore di OORFC piu' basso della soglia limite: %d%%", OORFC_limite));
				context.addIssue(1, ruleKey, String.format("Valore di OORFC COMPLESSIVO  più basso della soglia limite: %d%%", OORFC_limite));
			}else if(OORFC_minimo < 95){
				logger.info(String.format("Numero classi che rispettano RFC minimo: %d ", rfcMinimo));
				logger.info(String.format("Valore di OORFC piu' basso della soglia minima: %d%%", OORFC_minimo));
				context.addIssue(1, ruleKey, String.format("Valore di OORFC COMPLESSIVO più basso della soglia minima: %d%%", OORFC_minimo));
			}			
		}
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		classiAnalizzate++;
		rfc = 0;
		
		if (getExplicitConstructors(tree).isEmpty()) {
			rfc++; //vuol dire che la classe ha un costruttore implicito
		} else if(!getExplicitConstructors(tree).isEmpty()){
			rfc += getExplicitConstructors(tree).size();
		}
		
		if(rfc <= min){
			rfcMinimo++;
			rfcLimite++;
		} else if(rfc > min && rfc <= limit){
			rfcLimite++;
			context.addIssue(tree, ruleKey, String.format("Valore di rfc più alto della soglia minima: %d", rfc));
		} else 
			violazione = true;
		
		super.visitClass(tree);
	}	
	
	@Override
	public void visitVariable(VariableTree tree) {
		if(tree.initializer() != null){
			for (ModifierTree modifier : tree.modifiers()) {
				if(modifier.is(Kind.NEW_CLASS))
					rfc++;
			}
			super.visitVariable(tree);
		}
	}
	
	@Override
	public void visitMethod(MethodTree tree) {
		if(!tree.is(Kind.CONSTRUCTOR)){
			if( ((MethodTreeImpl)tree).getToken().getValue().equals("public") )
				rfc++;
		}
		super.visitMethod(tree);
	}
	
	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		ExpressionTree expressionTree = tree.methodSelect();
		if(expressionTree.is(Kind.MEMBER_SELECT)){
			//MemberSelectExpressionTree memberSelectExpressionTree = (MemberSelectExpressionTree) expressionTree;
			//System.out.println(memberSelectExpressionTree.identifier().name());
			rfc++;
		}
		super.visitMethodInvocation(tree);
	}
	
	//************************  METODI PRIVATI  ****************************
	private static boolean isConstructor(Tree tree) {
		return tree.is(Tree.Kind.CONSTRUCTOR);
	}
	
	private static List<Tree> getExplicitConstructors(ClassTree classTree) {
		ImmutableList.Builder<Tree> builder = ImmutableList.builder();
		for (Tree member : classTree.members()) {
			if (isConstructor(member)) {
					builder.add(member);
			}
		}
		return builder.build();
	}

}
