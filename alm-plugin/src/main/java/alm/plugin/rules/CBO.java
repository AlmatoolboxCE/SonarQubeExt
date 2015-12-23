package alm.plugin.rules;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.ArrayTypeTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.InstanceOfTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewArrayTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeCastTree;
import org.sonar.plugins.java.api.tree.TypeParameterTree;
import org.sonar.plugins.java.api.tree.UnionTypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.WildcardTree;

import alm.plugin.ALMExtensionDefinition;
import alm.plugin.utils.AlmPluginUtils;

import com.google.common.collect.Sets;


/**
 * 	Regola OOCBO – Tasso di coupling
 * 
 *	La regola quantifica la notazione relativa al “coupling” degli oggetti istanziati. 
 *	Per esempio, metodi di una Classe che usano metodi o variabili di istanza di un’altra Classe 
 *	al di fuori della gerarchia data dall’ereditarietà. 
 *	Un eccessivo coupling tra le Classi indica un cattivo disegno degli oggetti
 * 
 *	cboMinimo/cboLimite = numero di tipi di oggetti a cui fa riferimento una classe (istanze di oggetti presenti nella classe) 
 *				Il cboMinimo per ogni classe indica la soglia minima di classi a cui si può far riferimento
 *				Il cboLimite per ogni classe indica la soglia massima (soglia limite) di classi a cui si può far riferimento 
 *	
 * */

@Rule(key = CBO.KEY, 
name = "OOCBO – Tasso di coupling",
description = "Rilevazione delle classi correlate ad una classe al di fuori della gerarchia di ereditarietà",
tags = {"alm-sice"})
public class CBO extends BaseTreeVisitor implements JavaFileScanner {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String KEY = "CBO";
	private final RuleKey RULE_KEY = RuleKey.of(ALMExtensionDefinition.REPOSITORY_KEY, KEY);

	public static final int DEFAULT_MIN = 2;
	public static final int DEFAULT_LIMIT = 6;

	private Integer classiDaAnalizzare;
	private Integer classiAnalizzate;
	private Integer cboMinimo;
	private Integer cboLimite;
	private Integer OOCBO_minimo;
	private Integer OOCBO_limite;
	private boolean violazione;
	private JavaFileScannerContext context;

	private Set<String> types;
	private final Stack<Set<String>> nesting = new Stack<Set<String>>();

	@RuleProperty(key = "min",
			description = "Valore di soglia minimo per la profondità dell'albero di ereditarietà di una classe",
			defaultValue = "" + DEFAULT_MIN)
	private Integer min = DEFAULT_MIN;

	@RuleProperty(key = "limit",
			description = "Valore di soglia limite per la profondità dell'albero di ereditarietà una classe",
			defaultValue = "" + DEFAULT_LIMIT)
	private Integer limit = DEFAULT_LIMIT;

	public CBO() {
		logger.info("--- Analisi OOCBO ---");
		classiAnalizzate = new Integer(0);
		cboMinimo = new Integer(0);
		cboLimite = new Integer(0);
		OOCBO_minimo = new Integer(0);
		OOCBO_limite = new Integer(0);
		violazione = false;
	}

	@Override
	public void scanFile(JavaFileScannerContext context) {
		classiDaAnalizzare = Integer.parseInt(System.getProperty("numClassiDaAnalizzare"));
		this.context=context;
		scan(context.getTree());
		if(classiAnalizzate.equals(classiDaAnalizzare) ){
			logger.info(String.format("***OOCBO*** Classi analizzate: %d", classiAnalizzate));
			OOCBO_minimo = AlmPluginUtils.arrotonda((cboMinimo.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia minimo
			OOCBO_limite = AlmPluginUtils.arrotonda((cboLimite.doubleValue() / classiAnalizzate.doubleValue() ) * 100.00);  //Valore soglia limite

			if(violazione){  //se è presente anche una sola classe che non rispetta la soglia limite, si solleva la violazione
				logger.info(String.format("Numero classi che rispettano CBO limite: %d ", cboLimite));
				logger.info(String.format("Valore di OOCBO piu' basso della soglia limite: %d%%", OOCBO_limite));
				context.addIssue(1, RULE_KEY, String.format("Valore di OOCBO COMPLESSIVO più basso della soglia limite: %d%%", OOCBO_limite));
			}else if(OOCBO_minimo < 95){
				logger.info(String.format("Numero classi che rispettano CBO minimo: %d ", cboMinimo));
				logger.info(String.format("Valore di OOCBO piu' basso della soglia minima: %d%%", OOCBO_minimo));
				context.addIssue(1, RULE_KEY, String.format("Valore di OOCBO COMPLESSIVO più basso della soglia minima: %d%%", OOCBO_minimo));
			}				
		}
	}

	@Override
	public void visitClass(ClassTree tree) {
		classiAnalizzate++;
		if(tree.is(Kind.CLASS) && tree.simpleName() != null){
			nesting.push(types);
			types = Sets.newHashSet();
		}
		checkTypes(tree.superClass());
		checkTypes(tree.superInterfaces());
		super.visitClass(tree);
		if (tree.is(Tree.Kind.CLASS) && tree.simpleName() != null){
			//System.out.println(String.format("Classe %s; cbo: %d", tree.simpleName(), types.size()));
			if(types.size() <= min){
				cboMinimo++;
				cboLimite++;
			}				
			if(types.size() > min && types.size() <= limit){
				cboLimite++;
				context.addIssue(tree, RULE_KEY, String.format("Valore di OOCBO più alto della soglia minima: %d", types.size()));
			}
			if(types.size() > limit)
				violazione = true;
		}
	}

	@Override
	public void visitVariable(VariableTree tree) {
		checkTypes(tree.type());
		super.visitVariable(tree);
	}

	@Override
	public void visitCatch(CatchTree tree) {
		scan(tree.block());
	}

	@Override
	public void visitTypeCast(TypeCastTree tree) {
		checkTypes(tree.type());
		super.visitTypeCast(tree);
	}

	@Override
	public void visitMethod(MethodTree tree) {
		checkTypes(tree.returnType());
		super.visitMethod(tree);
	}

	@Override
	public void visitTypeParameter(TypeParameterTree typeParameter) {
		checkTypes(typeParameter.bounds());
		checkTypes(typeParameter.identifier());
		super.visitTypeParameter(typeParameter);
	}

	@Override
	public void visitUnionType(UnionTypeTree tree) {
		checkTypes(tree.typeAlternatives());
		super.visitUnionType(tree);
	}

	@Override
	public void visitParameterizedType(ParameterizedTypeTree tree) {
		checkTypes(tree.type());
		checkTypes(tree.typeArguments());
		super.visitParameterizedType(tree);
	}

	@Override
	public void visitNewClass(NewClassTree tree) {
		checkTypes(tree.typeArguments());
		if (tree.identifier().is(Tree.Kind.PARAMETERIZED_TYPE)) {
			scan(tree.enclosingExpression());
			checkTypes(((ParameterizedTypeTree) tree.identifier()).typeArguments());
			scan(tree.typeArguments());
			scan(tree.arguments());
			scan(tree.classBody());
		} else {
			super.visitNewClass(tree);
		}
	}

	@Override
	public void visitWildcard(WildcardTree tree) {
		checkTypes(tree.bound());
		super.visitWildcard(tree);
	}

	@Override
	public void visitArrayType(ArrayTypeTree tree) {
		checkTypes(tree.type());
		super.visitArrayType(tree);
	}

	@Override
	public void visitInstanceOf(InstanceOfTree tree) {
		checkTypes(tree.type());
		super.visitInstanceOf(tree);
	}

	@Override
	public void visitNewArray(NewArrayTree tree) {
		checkTypes(tree.type());
		super.visitNewArray(tree);
	}

	private void checkTypes(List<? extends Tree> types) {
		for (Tree type : types) {
			checkTypes(type);
		}
	}

	private void checkTypes(@Nullable Tree type){
		if (type == null || types == null) {
			return;
		}
		if(type.is(Kind.IDENTIFIER)){
			types.add( ((IdentifierTree)type).name() );
		} else if(type.is(Tree.Kind.MEMBER_SELECT)){
			ExpressionTree expr = (ExpressionTree) type;
			while (expr.is(Tree.Kind.MEMBER_SELECT)) {
				MemberSelectExpressionTree mse = (MemberSelectExpressionTree) expr;
				types.add(mse.identifier().name());
				expr = mse.expression();
			}
			if (expr.is(Tree.Kind.IDENTIFIER)) {
				types.add(((IdentifierTree) expr).name());
			}
			types.add(((MemberSelectExpressionTree) type).identifier().name());
		}
	}

}
