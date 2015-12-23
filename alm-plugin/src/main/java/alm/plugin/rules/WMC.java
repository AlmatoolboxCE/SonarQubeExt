package alm.plugin.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import alm.plugin.ALMExtensionDefinition;
import alm.plugin.utils.AlmPluginUtils;


/**
 * 
 * Regola OOWMC – Tasso di complessità delle classi
 * 
 * La regola WMC misura il numero dei metodi implementati in una classe che è una prima misura della complessità. 
 * Troppi metodi rendono la Classe di difficile comprensione e incrementano il rischio di errori a fronte di una modifica. 
 * 
 * */

@Rule(key = WMC.RULE_KEY,
		name = "OOWMC – Tasso di complessità delle classi",
		description = "Misura il numero dei metodi implementati in una classe. Troppi metodi rendono la Classe di difficile comprensione e incrementano il rischio di errori a fronte di una modifica.",
		tags = {"alm-sice"})
public class WMC extends BaseTreeVisitor implements JavaFileScanner {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String RULE_KEY = "Tasso di complessità";
	private final RuleKey ruleKey = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, RULE_KEY);
	 
	public static final int DEFAULT_METODI_MIN = 14;
	public static final int DEFAULT_COMPL_MIN = 70;
	public static final int DEFAULT_METODI_LIMIT = 20;
	public static final int DEFAULT_COMPL_LIMIT = 100;
	 
	private Integer classiDaAnalizzare;
	private Integer classiAnalizzate;
	private Integer wmcMinimo;
	private Integer wmcLimite;
	private Integer OOWMC_minimo;
	private Integer OOWMC_limite;
	private boolean violazione;
	 
	private JavaFileScannerContext context;
	 
	@RuleProperty(key = "metodiMin",
			 		description = "Valore di soglia minimo per il numero di metodi che deve avere una classe",
			 		defaultValue = "" + DEFAULT_METODI_MIN)
	private Integer metodiMin = DEFAULT_METODI_MIN;
	 
	@RuleProperty(key = "metodiLimit",
		 		description = "Valore di soglia limite per il numero di metodi che deve avere una classe",
		 		defaultValue = "" + DEFAULT_METODI_LIMIT)
	private Integer metodiLimit = DEFAULT_METODI_LIMIT;
	
	@RuleProperty(key = "complMin",
	 		description = "Valore di soglia minimo per la complessità ciclomatica che deve avere una classe",
	 		defaultValue = "" + DEFAULT_COMPL_MIN)
	private Integer complMin = DEFAULT_COMPL_MIN;
	
	@RuleProperty(key = "complLimit",
	 		description = "Valore di soglia limite per la complessità ciclomatica che deve avere una classe",
	 		defaultValue = "" + DEFAULT_COMPL_LIMIT)
	private Integer complLimit = DEFAULT_COMPL_LIMIT;
	
	public WMC() {
		logger.info("--- Analisi OOWMC ---");
		classiAnalizzate = new Integer(0);
		wmcMinimo = new Integer(0);
		wmcLimite = new Integer(0);
		OOWMC_minimo = new Integer(0);
		OOWMC_limite = new Integer(0);
		violazione = false;
	}

	@Override
	public void scanFile(JavaFileScannerContext ctx) {
		classiDaAnalizzare = Integer.parseInt(System.getProperty("numClassiDaAnalizzare"));
		this.context = ctx;
		scan(context.getTree());
		
		if (classiAnalizzate.equals(classiDaAnalizzare)){
			logger.info(String.format("***OOWMC*** Classi analizzate: %d", classiAnalizzate));
			OOWMC_minimo = AlmPluginUtils.arrotonda((wmcMinimo.doubleValue() / classiAnalizzate.doubleValue()) * 100.00);
			OOWMC_limite = AlmPluginUtils.arrotonda((wmcLimite.doubleValue() / classiAnalizzate.doubleValue()) * 100.00);
			
			if(violazione){  //se è presente anche una sola classe che non rispetta la soglia limite, si solleva la violazione
				logger.info(String.format("Numero classi che rispettano WMC limite: %d ", wmcLimite));
				logger.info(String.format("Valore di OOWMC piu' basso della soglia limite: %d%%", OOWMC_limite));
				context.addIssue(1, ruleKey, String.format("Valore di OOWMC COMPLESSIVO più basso della soglia limite: %d%%", OOWMC_limite));
			}else if(OOWMC_minimo < 95){
				logger.info(String.format("Numero classi che rispettano DIT minimo: %d ", wmcMinimo));
				logger.info(String.format("Valore di OOWMC piu' basso della soglia minima: %d%%", OOWMC_minimo));
				context.addIssue(1, ruleKey, String.format("Valore di OOWMC COMPLESSIVO più basso della soglia minima: %d%%", OOWMC_minimo));
			}
		}
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		classiAnalizzate++;
		int wmc = 0;
		int compl = 0;
		for (Tree iterable_element : tree.members()) {			
			if(iterable_element.is(Kind.METHOD)){
				wmc++;				
			}
		}
		compl = context.getComplexity(tree);
		if(wmc <= metodiMin && compl <= complMin){
			wmcMinimo++;
			wmcLimite++;
		} else if( (wmc > metodiMin && wmc <= metodiLimit) && (compl <= complLimit) ){
			wmcLimite++;
			context.addIssue(tree, ruleKey, String.format("Valore di wmc più alto della soglia minima: %d", wmc));
		} else if( (compl > complMin && compl <= complLimit) && (wmc <= metodiLimit) ){
			wmcLimite++;
			context.addIssue(tree, ruleKey, String.format("Valore di complessità più alto della soglia minima: %d", compl));
		} else
			violazione = true;
			
//		System.out.println(String.format("wmc della classe %s = %d",tree.simpleName().toString(), wmc));
//		System.out.println(String.format("complessità della classe %s = %d",tree.simpleName().toString(), compl));
		super.visitClass(tree);
	}

}
