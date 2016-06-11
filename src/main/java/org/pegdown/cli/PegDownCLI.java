package org.pegdown.cli;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * This class is the command line interface for PegDown. It simply converts one input file to a html output file.
 * 
 * @author Jan Ortner
 *
 */
public class PegDownCLI {

	/**
	 * The main method parses the arguments, reads the input file, transforms the content via a PegDownProcessor
	 * to a html string and writes this string to the output file.
	 * @param args the command line arguments (see {@link #printUsage()})
	 */
	public static void main(String[] args) {
		Path input=null;
		Path output=null;
		int extensions=Extensions.ALL;
		//parsing arguments
		for(int i=0; i<args.length;i++){
			System.out.println(args[i]);
			if(args[i].indexOf("+E")==0){ //add extension
				//System.out.println("add: "+args[i].substring(2, args[i].length())+" mask: "+Integer.toBinaryString(getExtensionMask(args[i].substring(2, args[i].length()))));
				extensions|=getExtensionMask(args[i].substring(2, args[i].length()));
			}else if(args[i].indexOf("-E")==0){ //remove extension
				//System.out.println("remove: "+args[i].substring(2, args[i].length())+" mask: "+Integer.toBinaryString(getExtensionMask(args[i].substring(2, args[i].length()))));
				extensions&=~getExtensionMask(args[i].substring(2, args[i].length()));
			}else if(input==null){ //inputfile
				input=Paths.get(args[i]);
			}else if(output==null){ //outputfile
				output=Paths.get(args[i]);
			}
		}
		if(input==null||!input.toFile().exists()){
			if(input==null)
				System.err.println("*** No input file given ***");
			if(!input.toFile().exists())
				System.err.println("*** File does not exist: "+input+" ***");
			printUsage();
			System.exit(1);
		}
		if(output==null){
			String fileName=input.getFileName().toString();
			fileName=fileName.substring(0, fileName.indexOf('.'))+".html";
			if(input.getParent()!=null)
				output=Paths.get(input.getParent().toString(), fileName);
			else
				output=Paths.get(fileName);
		}
		System.out.println("Reading file: "+input);
		System.out.println("Writing output to: "+output);
		System.out.println("ExtensionMask: "+Integer.toBinaryString(extensions));
		// do conversion
		try {
			List<String> inLines=Files.readAllLines(input);
			String in="";
			for(String line:inLines){
				in+=line+"\n";
			}
			PegDownProcessor proc=new PegDownProcessor(extensions);
			String out=proc.markdownToHtml(in);
			try {
				if(output.getParent()!=null)
					Files.createDirectory(output.getParent(), (FileAttribute<?>[]) null);
				Files.write(output, out.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE );
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the mask of the given extension. It reads the static variable with the given name from
	 * {@link Extensions} and returns the value of the field.
	 * @param extension the extension
	 * @return the mask or 0 if the extension was not found
	 */
	public static int getExtensionMask(String extension){
		try{
			Field f=Extensions.class.getField(extension);
			if((f.getModifiers()&Modifier.STATIC)!=0
					&& 	f.getType().equals(int.class)
					){
					return f.getInt(null);
				}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Prints the usage to the standard output stream.
	 */
	public static void printUsage(){
		String usage="Usage: \n";
		usage+="java -jar <jar-name> org.pegdown.cli.PegDownCLI [+/-E\"extension\"] <inputfile> [ouputfile]\n";
		usage+="\ninputfile:\n";
		usage+="The input file needs to be in markdownformat\n";
		usage+="\noutputfile:\n";
		usage+="The output file will be in html format\n";
		usage+="\nExtensions:\n";
		usage+="+ or - will select if an extension should be added or removed.\n";
		usage+="This is done in the order given by the arguments, initially ALL extensions are enabled.\n";
		usage+="Example:\n";
		usage+="-EALL - will remove all default extensions\n";
		usage+="Built-in extensions:\n";
		Field[] extensions=Extensions.class.getDeclaredFields();
		for(int i=0; i<extensions.length;i++){
			//basic sanity checks
			if((extensions[i].getModifiers()&Modifier.STATIC)!=0
				&& 	extensions[i].getType().equals(int.class)
				){
				usage+="\t"+extensions[i].getName()+"\n";
			}
		}
		System.out.println(usage);
	}
	
}
