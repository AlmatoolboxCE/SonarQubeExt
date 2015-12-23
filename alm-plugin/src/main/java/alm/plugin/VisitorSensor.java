package alm.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.java.Java;


@Phase(name = Phase.Name.PRE)
@DependsUpon("BEFORE_SQUID")
@DependedUpon(value="squid")
public class VisitorSensor implements Sensor{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	private final FileSystem fs;
	
	//Costruttore
	public VisitorSensor(CheckFactory checkFactory, Settings settings, FileSystem fs){
		this.fs = fs;
	}

	@Override
	public boolean shouldExecuteOnProject(Project project) {
		logger.info("***VisitorSensor*** " + fs.hasFiles(fs.predicates().hasLanguage(Java.KEY)));
		analyse(project, null);
		return fs.hasFiles(fs.predicates().hasLanguage(Java.KEY));
	}

	@Override
	public void analyse(Project module, SensorContext context) {
		
		int numClassiDaAnalizzare = 0;
		for (File sourceFile : getSourceFiles()) {
			numClassiDaAnalizzare++;
		}
		logger.info("***VisitorSensor*** Classi da analizzare = "+numClassiDaAnalizzare);
		System.setProperty("numClassiDaAnalizzare", String.valueOf(numClassiDaAnalizzare));
	}
	
	private Iterable<File> getSourceFiles(){
		return toFile(fs.inputFiles(fs.predicates().and(fs.predicates().hasLanguage(Java.KEY), fs.predicates().hasType(InputFile.Type.MAIN))));
	}
	
	private Iterable<File> toFile(Iterable<InputFile> inputFiles) {
		List<File> files = new ArrayList<File>();
		for (InputFile inputFile : inputFiles) {
		files.add(inputFile.file());
		}
		return files;
	}
	
}
