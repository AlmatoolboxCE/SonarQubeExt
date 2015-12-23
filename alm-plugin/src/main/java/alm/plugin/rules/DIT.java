package alm.plugin.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.declaration.ClassTreeImpl;
import org.sonar.java.resolve.Symbol;
import org.sonar.java.resolve.Type;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;

import alm.plugin.ALMExtensionDefinition;
import alm.plugin.utils.AlmPluginUtils;

/**
 * Regola DIT - Tasso di ereditariet�
 * 
 * Questa regola, va applicata ad ogni Classe dell'obiettivo e da un�indicazione sul numero delle Classi �ancestor� 
 * che possono potenzialmente influenzare la Classe.  
 * Pi� in profondit� si trova la Classe, pi� grande � il  numero di metodi che probabilmente pu� ereditare, 
 * rendendo pi� complessa la  comprensione del comportamento della Classe stessa
 * 
 * ditMinimo / ditLimite = indicano la profondit� di una classe, 
 * 							cio� quante classi eredita la suddetta classe (albero di ereditariet�)
 * 
 * */

@Rule(key = DIT.RULE_KEY,
		name = "OODIT � Tasso di ereditariet�",
		description = "Da un�indicazione sul numero delle Classi �ancestor� che possono potenzialmente influenzare la Classe",
		tags = {"alm-sice"})
public class DIT extends BaseTreeVisitor implements JavaFileScanner{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String RULE_KEY = "Tasso di ereditariet�";
	private final RuleKey ruleKey = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, RULE_KEY);
	 
	public static final int DEFAULT_MIN = 7;
	public static final int DEFAULT_LIMIT = 10;
	 
	private Integer classiDaAnalizzare;
	private Integer classiAnalizzate;
	private Integer ditMinimo;
	private Integer ditLimite;
	private Integer OODIT_minimo;
	private Integer OODIT_limite;
	private boolean violazione;
	 
	private JavaFileScannerContext context;
	 
	@RuleProperty(key = "min",
			 		description = "Valore di soglia minimo per la profondit� che deve avere la classe",
			 		defaultValue = "" + DEFAULT_MIN)
	private Integer min = DEFAULT_MIN;
	 
	@RuleProperty(key = "limit",
		 		description = "Valore di soglia limite per la profondit� che deve avere la classe",
		 		defaultValue = "" + DEFAULT_LIMIT)
	private Integer limit = DEFAULT_LIMIT;
	
	//Costruttore
	public DIT() {
		logger.info("--- Analisi OODIT ---");
		classiAnalizzate = new Integer(0);
		ditMinimo = new Integer(0);
		ditLimite = new Integer(0);
		OODIT_minimo = new Integer(0);
		OODIT_limite = new Integer(0);
		violazione = false;
	}
	 
	@Override
	public void scanFile(JavaFileScannerContext ctx) {
		classiDaAnalizzare = Integer.parseInt(System.getProperty("numClassiDaAnalizzare"));
		context = ctx;
		if (context.getSemanticModel() != null) {
			scan(context.getTree());
		}
		
		if (classiAnalizzate.equals(classiDaAnalizzare)) {
			logger.info(String.format("***OODIT*** Classi analizzate: %d", classiAnalizzate));
			OODIT_minimo = AlmPluginUtils.arrotonda((ditMinimo.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia minimo
			OODIT_limite = AlmPluginUtils.arrotonda((ditLimite.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia limite
			
			if(violazione){  //se � presente anche una sola classe che non rispetta la soglia limite, si solleva la violazione
				logger.info(String.format("Numero classi che rispettano DIT limite: %d ", ditLimite));
				logger.info(String.format("Valore di OODIT piu' basso della soglia limite: %d%%", OODIT_limite));
				context.addIssue(1, ruleKey, String.format("Valore di OODIT COMPLESSIVO pi� basso della soglia limite: %d%%", OODIT_limite));
			}else if(OODIT_minimo < 95){
				logger.info(String.format("Numero classi che rispettano DIT minimo: %d ", ditMinimo));
				logger.info(String.format("Valore di OODIT piu' basso della soglia minima: %d%%", OODIT_minimo));
				context.addIssue(1, ruleKey, String.format("Valore di OODIT COMPLESSIVO pi� basso della soglia minima: %d%%", OODIT_minimo));
			}			
		}
	}
	 
	@Override
	public void visitClass(ClassTree tree) {
		classiAnalizzate++;
		Symbol.TypeSymbol typeSymbol = ((ClassTreeImpl)tree).getSymbol();
		int dit = 0;
		while (typeSymbol.getSuperclass() != null) {
			dit++;
			typeSymbol = ((Type.ClassType)typeSymbol.getSuperclass()).getSymbol();
		}		
		if(dit <= min){
			ditMinimo++;
			ditLimite++;
		}
		if(dit > min && dit <= limit){
			ditLimite++;
			context.addIssue(1, ruleKey, String.format("Valore di DIT pi� alto della soglia minima: %d", dit));
		}
		if(dit > limit)
			violazione = true;
		
		super.visitClass(tree);
	}
	 
	 
}
