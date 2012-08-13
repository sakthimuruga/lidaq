package ie.deri.urq.lidaq.cli;


import ie.deri.urq.lidaq.cli.bench.BENCH_ARGUMENTS;
import ie.deri.urq.lidaq.cli.bench.BenchmarkCLI;
import ie.deri.urq.lidaq.log.LIDAQLOGGER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * This Main class parses all available classes in the JVM for command line objects having the specific PACKAGE_PREFIX.
 * 
 * @author juum
 *
 */
public class Bench extends CLIObject {
	private static final Logger logger = Logger.getLogger(Bench.class.getName());

	private static final String PACKAGE_PREFIX = "ie.deri.urq.lidaq.cli.bench";
	private static final String PATH_PREFIX = "/ie/deri/urq/lidaq/cli/bench";

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void addOptions(Options opts) {
		Class[] classes;
		try {
			classes = getClasses(PACKAGE_PREFIX);
			for(Class c: classes){
				Class cls = Bench.class.getClassLoader().loadClass(PACKAGE_PREFIX+"."+c.getSimpleName());
				if(BenchmarkCLI.class.isAssignableFrom(cls) && !cls.getSimpleName().equals("BenchmarkCLI")){
					BenchmarkCLI b = (BenchmarkCLI) cls.newInstance(); 
					for(Object o: b.getOption().getOptions()){
						opts.addOption((Option) o);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		
		opts.addOption(BENCH_ARGUMENTS.OPTION_SPARQL_QUERY);
		opts.addOption(BENCH_ARGUMENTS.OPTION_BENCHDIR);
		opts.addOption(BENCH_ARGUMENTS.OPTION_BENCHMARK);
	}

	
	protected void execute(CommandLine cmd) {
		try {
			if(!CLIObject.hasOption(cmd,BENCH_ARGUMENTS.PARAM_BENCHMARK)) {
				usage();
			}
			else{
//				LIDAQLOGGER.setDefaultLogging();
				if(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_BENCHDIR) == null)
					usage("No benchmark directory specified!");
				if(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_SPARQL_QUERY) == null)
					usage("No benchmark query/queries specified!");
				
				File benchDir = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_BENCHDIR));
				File query = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_SPARQL_QUERY));
				
				BenchmarkCLI bench = (BenchmarkCLI) Class.forName(PACKAGE_PREFIX + "."+CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_BENCHMARK)).newInstance();
				bench.setup();
				bench.execute(benchDir, query, cmd);
				bench.shutdown();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void usage() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		StringBuffer sb = new StringBuffer();
		sb.append("  -?\t\t\tprint help screen\n");
		sb.append("  -h,--help\t\tprint help screen\n");		 
		sb.append("  -bd,--benchDir <dir>  benchmark directory\n");		 
		sb.append("  -q,--query <query>    sparql query\n");
		sb.append("  -b,--bench <bench>\tbenchmark; one of the following\n");	
		
		Class [] classes = getClasses(PACKAGE_PREFIX);
		for(Class c: classes){
			Class cls = Bench.class.getClassLoader().loadClass(PACKAGE_PREFIX+"."+c.getSimpleName());
			if(BenchmarkCLI.class.isAssignableFrom(cls) && !cls.getSimpleName().equals("BenchmarkCLI")){
				BenchmarkCLI o = (BenchmarkCLI) cls.newInstance(); 
				sb.append("\n\t").append(o.getCommand()).append(" -- ").append(o.getDescription());
			}
		}
		sb.append("\n_________________________");
		usage(sb.toString());
	}
	
	private static void usage(String msg) {
		logger.info("\n"+msg);
	}
	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		String uri;
		ArrayList<Class> classes = new ArrayList<Class>();
		try {
			uri = Bench.class.getResource(PATH_PREFIX).toURI().toASCIIString();
			if(uri.startsWith("jar:file:")){
				classes = classesFromJar(uri);
			}
			else{
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				String path = packageName.replace('.', '/');
				Enumeration<URL> resources = classLoader.getResources(path);
				List<File> dirs = new ArrayList<File>();
				while (resources.hasMoreElements()) {
					URL resource = resources.nextElement();
					dirs.add(new File(resource.getFile()));
				}
				for (File directory : dirs) {
					classes.addAll(findClasses(directory, packageName));
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//		
		//		
		return classes.toArray(new Class[classes.size()]);
	}
	private static ArrayList<Class> classesFromJar(String uri) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();
		String jarURI = uri.substring("jar:file:".length(),uri.lastIndexOf("!"));
		JarInputStream jarFile = new JarInputStream(new FileInputStream(jarURI));
		JarEntry jarEntry;
		while (true) {
			jarEntry = jarFile.getNextJarEntry();
			//            System.out.println(jarEntry);
			if (jarEntry == null) {
				break;
			}
			if ((jarEntry.getName().startsWith(PACKAGE_PREFIX.replace(".", "/"))) &&
					(jarEntry.getName().endsWith(".class"))) {
				String classEntry = jarEntry.getName().replaceAll("/", "\\.");
				classes.add(Class.forName(classEntry.substring(0, classEntry.indexOf(".class"))));
			}
		}
		return classes;
	}
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				if(!file.getName().equals("BenchmarkCLI.class"))
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}
