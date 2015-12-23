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
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import alm.plugin.ALMExtensionDefinition;
import alm.plugin.utils.AlmPluginUtils;

/**
 * 	Regola OOLOCM – Tasso di Coesione dei Metodi e complessità ciclomatica
 * 
 *	viene rilevato il rapporto tra la sommatoria del numero di metodi che accedono ad una variabile (Attributo) 
 * 	e il numero dei metodi moltiplicato gli attributi della classe stessa 
 * 
 *	M = Numero dei Metodi di una Classe 
 *	A= Numero di Variabili (Attributo) di una Classe
 *	mj = Numero di Metodi che accedono ad una Variabile (Attributo) 
 * */
@Rule(key = CoesioneMetodi.KEY, 
			name = "OOLOCM – Tasso di Coesione dei Metodi e complessità ciclomatica ", 
			description = "Misura del grado di diversità fra i metodi di una classe in riferimento alle variabili o agli attributi degli stessi",
			tags = {"alm-sice"})
public class CoesioneMetodi extends BaseTreeVisitor implements JavaFileScanner{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private JavaFileScannerContext context;
	
	public static final String KEY = "LOCM";
	private final RuleKey RULE_KEY = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, KEY);
	
	public static final int DEFAULT_MIN = 75;
	public static final int DEFAULT_COMPL_MIN = 70;
	public static final int DEFAULT_LIMIT = 75;
	public static final int DEFAULT_COMPL_LIMIT = 100;
	
	private Integer numClassiAnalizzate;
	private Integer numClassiDaAnalizzare;
	private Integer numMetodi;
	private Integer numVariabili;
	private Integer mJ;
	private Integer numClassiRispettanoLocmMinimo;
	private Integer numClassiRispettanoLocmLimite;
	Integer OOLOCM_minimo = 0;
	Integer OOLOCM_limite = 0;
	
	@RuleProperty(key = "min",
	 		description = "Valore di soglia minimo per la coesione metodi di una classe",
	 		defaultValue = "" + DEFAULT_MIN)
	private Integer min = DEFAULT_MIN;
	
	@RuleProperty(key = "limit",
	 		description = "Valore di soglia limite per la la coesione metodi di una classe",
	 		defaultValue = "" + DEFAULT_LIMIT)
	private Integer limit = DEFAULT_LIMIT;
	
	@RuleProperty(key = "complMin",
	 		description = "Valore di soglia minimo per la complessità ciclomatica che deve avere una classe",
	 		defaultValue = "" + DEFAULT_COMPL_MIN)
	private Integer complMin = DEFAULT_COMPL_MIN;
	
	@RuleProperty(key = "complLimit",
	 		description = "Valore di soglia limite per la complessità ciclomatica che deve avere una classe",
	 		defaultValue = "" + DEFAULT_COMPL_LIMIT)
	private Integer complLimit = DEFAULT_COMPL_LIMIT;
	
	public CoesioneMetodi() {
		logger.info("--- Analisi OOLOCM ---");
		numClassiAnalizzate = new Integer(0);
		numClassiRispettanoLocmMinimo = new Integer(0);
		numClassiRispettanoLocmLimite = new Integer(0);
	}
	
	@Override
	public void scanFile(JavaFileScannerContext context) {
		numClassiDaAnalizzare = new Integer(System.getProperty("numClassiDaAnalizzare"));
		this.context = context;
		
		scan(this.context.getTree());
		
		/** Una volta che ho analizzato tutte le classi e raccolto i valori di LOCM
		 *  faccio la media insieme alla complessità di ogni classe*/
		if(numClassiAnalizzate.equals(numClassiDaAnalizzare)){
			logger.info(String.format("***OOLOCM*** Classi analizzate: %d", numClassiAnalizzate));
			OOLOCM_minimo = AlmPluginUtils.arrotonda((numClassiRispettanoLocmMinimo.doubleValue() / numClassiAnalizzate.doubleValue()) * 100.00); //Valore soglia minimo
			OOLOCM_limite = AlmPluginUtils.arrotonda((numClassiRispettanoLocmLimite.doubleValue() / numClassiAnalizzate.doubleValue()) * 100.00); //Valore soglia limite
			
			if(OOLOCM_minimo < 95){				
				if(OOLOCM_limite != 100){
					logger.info(String.format("Numero classi che rispettano OOLOCM limite: %d ", numClassiRispettanoLocmLimite));
					logger.info(String.format("Valore di OOLOCM piu' basso della soglia limite: %d%%", OOLOCM_limite));
					context.addIssue(1, RULE_KEY, String.format("Il valore di OOLOCM COMPLESSIVO non rispetta la soglia limite : %d%%", OOLOCM_limite));
				}
				else{
					logger.info(String.format("Numero classi che rispettano OOLOCM minimo: %d ", numClassiRispettanoLocmMinimo));
					logger.info(String.format("Valore di OOLOCM piu' basso della soglia minima: %d%%", OOLOCM_minimo));
					context.addIssue(1, RULE_KEY, String.format("Il valore di OOLOCM COMPLESSIVO non rispetta la soglia minima: %d%%", OOLOCM_minimo));
				}
			}
		}
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		numClassiAnalizzate++;
		if (!tree.is(Kind.INTERFACE) &&  tree.simpleName() != null ) {
			numMetodi = 0;
			numVariabili = 0;
			mJ = 0;
			
			for (Tree elemento : tree.members()) {
				if (elemento.is(Kind.VARIABLE)) {
					numVariabili++;
				}

				if (elemento.is(Kind.METHOD) && ((MethodTree)elemento).block() != null) {
					numMetodi++;
					for (StatementTree elementoMetodo : ((MethodTree) elemento).block().body()) {
						if (elementoMetodo.is(Kind.EXPRESSION_STATEMENT)) {
							mJ++;
						}
					}
				}
			}
			int locm = Math.round(calcolaLOCM( ((ClassTree) tree).simpleName().name()).floatValue() );
			if (locm >= min && this.context.getComplexity(tree) <= complMin)
				numClassiRispettanoLocmMinimo++;
			if (locm >= limit && this.context.getComplexity(tree) <= complLimit)
				numClassiRispettanoLocmLimite++;
			super.visitClass(tree);
		}
	}

	
	private Double calcolaLOCM(String nomeClasse){
		Double ris = 100.0;
		
		if(numMetodi!=0 && numVariabili!=0){			
			/**   Formula per il LOCM di classe   */
			Integer prodotto = numMetodi * numVariabili;
			double rapporto = (mJ.doubleValue()) / prodotto.doubleValue();
			ris = (1 - rapporto)*100;
		}
		//logger.info(String.format("Valore di LOCM ritornato per la classe %s: %.2f", nomeClasse, ris));
		return ris;
	}
}
