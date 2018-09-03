package br.ufes.inf.lprm.ReactiveCodeGenerate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Teste {
	String out = "";
	
	public Teste (String text) {
		this.out = text;
	}
	
	
	public void startEvent () {
			
	}
	
	public void endEvent () {
		
	}
	
	public void parDo () {
		
	}

	public void loopDo (BufferedWriter buff) {
		
	}
	
	
	public void fileGenerator(String path) throws IOException {
		
		if (path == "Teste") {	
	        BufferedWriter buffWrite = new BufferedWriter(new FileWriter("C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/blink.terra"));
	        
	        buffWrite.append("#include \"TerraNet.defs\"" + "\n\n");
	        buffWrite.append("par do" + "\n");        
		        buffWrite.append("\t" + "loop do" + "\n");
		        	buffWrite.append("\t\t" + "await 1s;" + "\n");
		        	buffWrite.append("\t\t" + "emit LED0(TOGGLE);" + "\n");
		        buffWrite.append("\t" + "end" + "\n"); 
	        buffWrite.append("with" + "\n");
		        buffWrite.append("\t" + "loop do" + "\n");
			    	buffWrite.append("\t\t" + "await 2s;" + "\n");
			    	buffWrite.append("\t\t" + "emit LED1(TOGGLE);" + "\n");
			    buffWrite.append("\t" + "end" + "\n");        	
	        buffWrite.append("with" + "\n");
		        buffWrite.append("\t" + "loop do" + "\n");
			    	buffWrite.append("\t\t" + "await 4s;" + "\n");
			    	buffWrite.append("\t\t" + "emit LED2(TOGGLE);" + "\n");
			    buffWrite.append("\t" + "end" + "\n");        
	        buffWrite.append("end");
	
	        // buffWrite.append(this.out);
	        buffWrite.close();
		} else {
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter("C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/photo_temp.terra"));
	        
	        buffWrite.append("var ushort tValue,pValue;" + "\n");
	        buffWrite.append("loop do" + "\n"); 
		        buffWrite.append("\t" + "par/and do" + "\n");
		        	buffWrite.append("\t\t" + "emit REQ_PHOTO();" + "\n");
		        	buffWrite.append("\t\t" + "pValue = await PHOTO;" + "\n");
		        buffWrite.append("\t" + "with" + "\n"); 
			    	buffWrite.append("\t\t" + "emit REQ_TEMP();" + "\n");
			    	buffWrite.append("\t\t" + "tValue = await TEMP;" + "\n");
			    buffWrite.append("\t" + "end" + "\n");
			    buffWrite.append("\t" + "if pValue > 200 or tValue > 300 then" + "\n");
			    	buffWrite.append("\t\t" + "emit LED0(ON);" + "\n");
			    buffWrite.append("\t" + "end" + "\n");
			    buffWrite.append("\t" + "await 1min;" + "\n");
			    buffWrite.append("\t" + "emit LED0(OFF);" + "\n");
	        buffWrite.append("end");
	
	        // buffWrite.append(this.out);
	        buffWrite.close();
		}
    }
	
}
