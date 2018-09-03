package br.ufes.inf.lprm.ReactiveCodeGenerate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.text.html.parser.TagElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.hamcrest.core.IsInstanceOf;
import org.ow2.orchestra.jaxb.bpmn.*;
import org.ow2.orchestra.jaxb.bpmn.bpmndi.BPMNDiagram;
import org.ow2.orchestra.jaxb.bpmn.bpmndi.BPMNEdge;
import org.ow2.orchestra.jaxb.bpmn.bpmndi.BPMNPlane;
import org.ow2.orchestra.jaxb.bpmn.bpmndi.BPMNShape;
import org.ow2.orchestra.jaxb.bpmn.di.DiagramElement;
import org.xml.sax.SAXException;

import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.schemagen.xmlschema.List;


public class App {
	
	private static final String MODEL = "C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/BlinkTerraGen.bpmn2";
	//private static final String MODEL = "C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/photo_temp.bpmn2";
	//private static final String MODEL = "C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/Exemplo1Gen.bpmn2";
	//private static final String MODEL = "C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/Exemplo1.bpmn2";
	
    public static void main( String[] args ) throws JAXBException, IOException, SAXException {
    	
    	// inicializa arquivo para escrita
    	BufferedWriter buffWrite = new BufferedWriter(new FileWriter("C:/Users/Agrizzi/eclipse-workspace/ReactiveCodeGenerate/src/main/java/br/ufes/inf/lprm/Resource/test.terra"));
    	
    	// GET BPMN MODEL FROM FILE
    	File model = new File(MODEL);
    	Definitions def = JaxbUtil.unmarshalBpmn(model);
    	
    	TBaseElement saida = null, saidaTimer = null, escape = null;
    	TInclusiveGateway saidaIF = null;
    	Boolean notIfElse = true, notIfExem3 = true;
    	Boolean subProOut = true;
    	HashMap<String,TSequenceFlow> flows = new HashMap<String,TSequenceFlow>();
    	HashMap<String,String> types = new HashMap<String,String>();
    	
    	// BPMN PROCESS
    	for (int k = 0; k < def.getRootElements().size(); k++) {
    		
    		// pega todos as definições
    		if (def.getRootElements().get(k).getDeclaredType().isAssignableFrom(TItemDefinition.class)) {
    			TItemDefinition itemDef = (TItemDefinition) def.getRootElements().get(k).getValue();
    			//System.out.println(itemDef.getId());
    			types.put(itemDef.getId(),itemDef.getStructureRef().getLocalPart());
    		}
			
    		// pega o processo como todo
    		if (def.getRootElements().get(k).getDeclaredType().isAssignableFrom(TProcess.class)) {
				TProcess process = (TProcess) def.getRootElements().get(k).getValue();
				
				// Armazena os SequeceFlows
				for (int i = 0; i < process.getFlowElements().size(); i++) {
	    			TFlowElement Element = (TFlowElement) process.getFlowElements().get(i).getValue();		
	    			
	    			// pega todos os Sequence Flow
	    			if (Element.getId().startsWith("SequenceFlow")) {
	    				TSequenceFlow sequence = (TSequenceFlow) Element;
	    				flows.put(sequence.getId(),sequence);
	    				
	    				TBaseElement source = (TBaseElement) sequence.getSourceRef();
	    				TBaseElement target = (TBaseElement) sequence.getTargetRef();
	    				
	    				
	    				System.out.println(source.getId() + " ----"+sequence.getId()+"----> " + target.getId());
					}
	    			
				}
				
				// Pega dentro do processo, o fluxo.
				for (int i = 0; i < process.getFlowElements().size(); i++) {
	    			TFlowElement Element = (TFlowElement) process.getFlowElements().get(i).getValue();		
	    			
	    			
	    			// pega todos os starts Events fora de subProcessos
	    			if (Element.getId().startsWith("StartEvent")) {
	    				TStartEvent start = (TStartEvent) Element;
	    				
	    				
	    				if (start.getProperties().size() > 0) {
	    					for (int j = 0; j < start.getProperties().size(); j++) {
		    					if (start.getProperties().get(j).getItemSubjectRef() != null) {
		    						buffWrite.append("var " + types.get(start.getProperties().get(j).getItemSubjectRef().toString()) +
		    								" " + start.getProperties().get(j).getName() + "; \n");
		    					} else {
		    						buffWrite.append(start.getProperties().get(j).getName() + "\n");
		    					}
							}
	    					buffWrite.append("\n");
	    				}
	    				
	    				// pega todos os sequences flows que sai do start event
	    				for (int j = 0; j < start.getOutgoings().size(); j++) {
	    					TSequenceFlow seq = (TSequenceFlow) flows.get(start.getOutgoings().get(j).toString());
	    					if (seq != null) {
	    						TBaseElement target = (TBaseElement) seq.getTargetRef();
	    						
	    						// Verificar qual elemento basico é esse para processguir.
	    						
	    						if (target instanceof TComplexGateway) {
		    						TComplexGateway complex = (TComplexGateway) target;
		    						subProOut = false;
		    						buffWrite.append("par do" + "\n");
		    						for (int r = 0; r < complex.getOutgoings().size(); r++) {
		    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(complex.getOutgoings().get(r).toString());
		    							if (seqOut != null) {
		    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
		    								TSubProcess sub = (TSubProcess) out;
		    								buffWrite.append("\t" + "loop do" + "\n");
		    								
		    								// BLOCO DE SUBPROCESSO
		    								for (int s = 0; s < sub.getFlowElements().size(); s++) {
		    			    					TFlowElement subElement = (TFlowElement) sub.getFlowElements().get(s).getValue();
		    			    					
		    			    					// Armazena os SequeceFlows do SUBPROCESSO
		    									for (int sp = 0; sp < sub.getFlowElements().size(); sp++) {
		    						    			TFlowElement subElementIn = (TFlowElement) sub.getFlowElements().get(sp).getValue();		
		    						    			
		    						    			// pega todos os Sequence Flow
		    						    			if (subElementIn.getId().startsWith("SequenceFlow")) {
		    						    				TSequenceFlow subSequence = (TSequenceFlow) subElementIn;
		    						    				flows.put(subSequence.getId(),subSequence);
		    										}
		    									}	  
		    			    					
		    			    					if (subElement.getId().startsWith("StartEvent")) {
		    					    				TStartEvent startIn = (TStartEvent) subElement;
		    				    				
		    					    				// pega todos os sequences flows que sai do start event dentro do subprocesso
		    					    				for (int b = 0; b < startIn.getOutgoings().size(); b++) {
		    					    					TSequenceFlow seqIn = (TSequenceFlow) flows.get(startIn.getOutgoings().get(b).toString());
		    					    					if (seqIn != null) {
		    					    						TBaseElement targetIn = (TBaseElement) seqIn.getTargetRef();
		    					    						
		    					    						// Verificar qual elemento basico é esse para processguir.
		    					    						
		    					    						if (targetIn instanceof TIntermediateCatchEvent) {
		    				    								TIntermediateCatchEvent event = (TIntermediateCatchEvent) targetIn;
		    				    								TTimerEventDefinition timer =  (TTimerEventDefinition) event.getEventDefinitions().get(0).getValue();
		    				    								buffWrite.append("\t\t" + "await " + timer.getTimeDuration().getContent().get(0).toString() + "; \n");

		    				    								TSequenceFlow prox = (TSequenceFlow) flows.get(event.getOutgoings().get(0).toString());
		    					    							if (prox != null) {
		    					    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
		    					    								if (eleNext instanceof TScriptTask) {
		    					    									TScriptTask script = (TScriptTask) eleNext;
		    					    									buffWrite.append("\t\t" + script.getScript().getContent().get(0).toString() + "\n");
		    					    									
		    					    									TSequenceFlow next = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    					    									if (next != null) {
		    					    										TBaseElement fim = (TBaseElement) next.getTargetRef();
		    					    										if (fim instanceof TEndEvent) {
		    					    											TEndEvent end = (TEndEvent) fim;
		    					    											//buffWrite.append("end");
		    					    										}
		    					    									}
		    					    								}
		    					    							}
		    			    								}
		    					    						
		    					    						if (targetIn instanceof TComplexGateway) {
		    						    						TComplexGateway complexIn = (TComplexGateway) target;
		    						    						buffWrite.append("\t" + "par do" + "\n");
		    						    						for (int n = 0; n < complexIn.getOutgoings().size(); n++) {
		    						    							TSequenceFlow seqOutIn = (TSequenceFlow) flows.get(complexIn.getOutgoings().get(n).toString());
		    						    							if (seqOut != null) {
		    						    								TBaseElement outIn = (TBaseElement) seqOut.getTargetRef();
		    						    								System.out.println(outIn.getId());
		    						    							}
		    						    							
		    						    							if ((n+1) == complex.getOutgoings().size()) {
		    						    								buffWrite.append("\t" + "end" + "\n");	
		    						    							} else {
		    						    								buffWrite.append("\t" + "with" + "\n");	
		    						    							}
		    						    						}
		    					    						}
		    					    						if (targetIn instanceof TExclusiveGateway) {
		    					    							TExclusiveGateway exclu = (TExclusiveGateway) target;
		    					    							buffWrite.append("\t" + "par/or do" + "\n");
		    						    						for (int m = 0; m < exclu.getOutgoings().size(); m++) {
		    						    							TSequenceFlow seqOutIn = (TSequenceFlow) flows.get(exclu.getOutgoings().get(m).toString());
		    						    							if (seqOutIn != null) {
		    						    								TBaseElement outIn = (TBaseElement) seqOutIn.getTargetRef();
		    						    								System.out.println(outIn.getId());
		    						    							}
		    						    						}
		    					    						}
		    					    						if (targetIn instanceof TParallelGateway) {
		    					    							TParallelGateway para = (TParallelGateway) target;
		    					    							buffWrite.append("\t" + "par/and do" + "\n");
		    						    						for (int v = 0; v < para.getOutgoings().size(); v++) {
		    						    							TSequenceFlow seqOutIn = (TSequenceFlow) flows.get(para.getOutgoings().get(r).toString());
		    						    							if (seqOutIn != null) {
		    						    								TBaseElement outIn = (TBaseElement) seqOutIn.getTargetRef();
		    						    								TScriptTask task = (TScriptTask) outIn;
		    						    								buffWrite.append("\t\t" + task.getScript().getContent().get(0).toString() + "\n");
		    						    								
		    						    								if (task.getOutgoings().size() != 0) {
		    						    									TSequenceFlow next = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
		    						    									if (next != null) {
		    						    										TBaseElement eleNext = (TBaseElement) next.getTargetRef();
		    						    										TIntermediateCatchEvent interCatch = (TIntermediateCatchEvent) eleNext;
		    						    										buffWrite.append("\t\t" + interCatch.getProperties().get(0).getName().toString() + "\n");
		    						    										
		    						    										if (interCatch.getOutgoings().size() != 0) {
		    						    											TSequenceFlow nextTo = (TSequenceFlow) flows.get(interCatch.getOutgoings().get(0).toString());
		    						    											if (nextTo != null) {
		    						    												TBaseElement end = (TBaseElement) nextTo.getTargetRef();
		    						    												if (end instanceof TParallelGateway) {
		    						    													saida = end;
		    						    												}
		    						    											}
		    						    										}
		    						    									}
		    						    								}
		    						    								
		    						    							}
		    						    							if ((r+1) == para.getOutgoings().size()) {
		    						    								buffWrite.append("\t" + "end" + "\n");	
		    						    							} else {
		    						    								buffWrite.append("\t" + "with" + "\n");	
		    						    							}
		    						    						}
		    					    						}
		    					    						if (saida != null) {
		    					    							TGateway gate = (TGateway) saida;
		    					    							TSequenceFlow next = (TSequenceFlow) flows.get(gate.getOutgoings().get(0).toString());
		    					    							if (next != null) {
		    					    								TBaseElement eleNext = (TBaseElement) next.getTargetRef();
		    					    								TScriptTask task = (TScriptTask) eleNext;
		    					    								TSequenceFlow prox = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
		    					    								if (prox != null) {
		    					    									TBaseElement inclu = (TBaseElement) prox.getTargetRef();
		    					    									if (inclu instanceof TInclusiveGateway) {
		    					    										buffWrite.append("\t" + task.getScript().getContent().get(0).toString() + "\n");
		    					    										TInclusiveGateway inclusive = (TInclusiveGateway) inclu;
		    					    										for (int fi = 0; fi < inclusive.getOutgoings().size(); fi++) {
		    							    									TSequenceFlow nextTo = (TSequenceFlow) flows.get(inclusive.getOutgoings().get(fi).toString());
		    							    									if (nextTo != null) {
		    									    								TBaseElement hasInclusive = (TBaseElement) nextTo.getTargetRef();
		    									    								if (hasInclusive instanceof TInclusiveGateway) {
		    									    									TInclusiveGateway notElse = (TInclusiveGateway) hasInclusive;
		    									    									saidaIF = notElse;
		    									    									notIfElse = false;
		    								    									}
		    									    								if (notIfElse) {
		    									    									buffWrite.append("\t" + "else" + "\n");
		    									    								}
		    									    								if (hasInclusive instanceof TScriptTask) {
		    									    									TScriptTask script = (TScriptTask) hasInclusive;
		    									    									buffWrite.append("\t\t" + script.getScript().getContent().get(0).toString() + "\n");
		    									    									TSequenceFlow flowInclu = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    									    									saidaIF = (TInclusiveGateway) flowInclu.getTargetRef();
		    									    								}
		    							    									}
		    																}
		    					    										buffWrite.append("\t" + "end" + "\n");
		    					    									}
		    					    								}
		    					    							}
		    					    							if (saidaIF != null) {
		    					    								TSequenceFlow prox = (TSequenceFlow) flows.get(saidaIF.getOutgoings().get(0).toString());
		    					    								if (prox != null) {
		    						    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
		    						    								if (eleNext instanceof TIntermediateCatchEvent) {
		    							    								TIntermediateCatchEvent event = (TIntermediateCatchEvent) eleNext;
		    							    								TTimerEventDefinition timer =  (TTimerEventDefinition) event.getEventDefinitions().get(0).getValue();
		    							    								buffWrite.append("\t" + "await " + timer.getTimeDuration().getContent().get(0).toString() + "; \n");
		    							    								//System.out.println(timer.getTimeDuration().getContent().get(0).toString());
		    							    								saidaTimer = event;
		    						    								}
		    					    								}
		    					    							}
		    					    						}
		    					    						if (saidaTimer != null) {
		    					    							TIntermediateCatchEvent event = (TIntermediateCatchEvent) saidaTimer;
		    					    							TSequenceFlow prox = (TSequenceFlow) flows.get(event.getOutgoings().get(0).toString());
		    					    							if (prox != null) {
		    					    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
		    					    								if (eleNext instanceof TScriptTask) {
		    					    									TScriptTask script = (TScriptTask) eleNext;
		    					    									buffWrite.append("\t" + script.getScript().getContent().get(0).toString() + "\n");
		    					    									
		    					    									TSequenceFlow next = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    					    									if (next != null) {
		    					    										TBaseElement fim = (TBaseElement) next.getTargetRef();
		    					    										if (fim instanceof TEndEvent) {
		    					    											TEndEvent end = (TEndEvent) fim;
		    					    											buffWrite.append("end");
		    					    										}
		    					    									}
		    					    								}
		    					    							}
		    					    						}
		    					    					}
		    										}
		    				    				}

		    								}
		    								buffWrite.append("\t" + "end" + "\n");
		    							}
		    							
		    							if ((r+1) == complex.getOutgoings().size()) {
		    								buffWrite.append("end" + "\n");	
		    							} else {
		    								buffWrite.append("with" + "\n");	
		    							}
		    						}
	    						}
	    						if (target instanceof TExclusiveGateway) {
	    							TExclusiveGateway exclu = (TExclusiveGateway) target;
		    						for (int r = 0; r < exclu.getOutgoings().size(); r++) {
		    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(exclu.getOutgoings().get(r).toString());
		    							if (seqOut != null) {
		    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
		    								//System.out.println(out.getId());
		    							}
		    						}
	    						}
	    						if (target instanceof TParallelGateway) {
	    							TParallelGateway para = (TParallelGateway) target;
		    						for (int r = 0; r < para.getOutgoings().size(); r++) {
		    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(para.getOutgoings().get(r).toString());
		    							if (seqOut != null) {
		    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
		    								//System.out.println(out.getId());
		    							}
		    						}
	    						}
	    						
	    						// EXEMPLO 3
	    						if (target instanceof TScriptTask) {
	    							TScriptTask script = (TScriptTask) target;
	    							buffWrite.append(script.getScript().getContent().get(0).toString() + "\n\n");
	    						}
	    					}
						}
					}
	    			
	    			
	    			
	    	
	    			
	    			//  SUBPROCESSOS dentro do processo
	    			if ((Element.getId().startsWith("SubProcess")) && (subProOut)) {
	    				TSubProcess sub = (TSubProcess) Element;
	    				buffWrite.append("loop do" + "\n");
	    				
	    				// Armazena os SequeceFlows do SUBPROCESSO
						for (int sp = 0; sp < sub.getFlowElements().size(); sp++) {
			    			TFlowElement subElement = (TFlowElement) sub.getFlowElements().get(sp).getValue();		
			    			
			    			// pega todos os Sequence Flow
			    			if (subElement.getId().startsWith("SequenceFlow")) {
			    				TSequenceFlow subSequence = (TSequenceFlow) subElement;
			    				flows.put(subSequence.getId(),subSequence);
			    				
			    				TBaseElement source = (TBaseElement) subSequence.getSourceRef();
			    				TBaseElement target = (TBaseElement) subSequence.getTargetRef();
			    				
			    				
			    				//System.out.println(source.getId() + " ----"+subSequence.getId()+"----> " + target.getId());
							}
			    			
						}
	    				
	    				
	    				for (int s = 0; s < sub.getFlowElements().size(); s++) {
	    					TFlowElement subElement = (TFlowElement) sub.getFlowElements().get(s).getValue();
	    					
	    				
	    				if (subElement.getId().startsWith("StartEvent")) {
		    				TStartEvent start = (TStartEvent) subElement;
	    				
		    				// pega todos os sequences flows que sai do start event dentro do subprocesso
		    				for (int j = 0; j < start.getOutgoings().size(); j++) {
		    					TSequenceFlow seq = (TSequenceFlow) flows.get(start.getOutgoings().get(j).toString());
		    					if (seq != null) {
		    						TBaseElement target = (TBaseElement) seq.getTargetRef();
		    						
		    						// Verificar qual elemento basico é esse para processguir.
		    						
		    						if (target instanceof TIntermediateCatchEvent) {
	    								TIntermediateCatchEvent event = (TIntermediateCatchEvent) target;
	    								TTimerEventDefinition timer =  (TTimerEventDefinition) event.getEventDefinitions().get(0).getValue();
	    								buffWrite.append("\t" + "await " + timer.getTimeDuration().getContent().get(0).toString() + "; \n");

	    								TSequenceFlow prox = (TSequenceFlow) flows.get(event.getOutgoings().get(0).toString());
		    							if (prox != null) {
		    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
		    								if (eleNext instanceof TScriptTask) {
		    									TScriptTask script = (TScriptTask) eleNext;
		    									buffWrite.append("\t" + script.getScript().getContent().get(0).toString() + "\n");
		    									
		    									TSequenceFlow next = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    									if (next != null) {
		    										TBaseElement sequence = (TBaseElement) next.getTargetRef();
		    										if (sequence instanceof TIntermediateCatchEvent) {
		    											TIntermediateCatchEvent inter = (TIntermediateCatchEvent) sequence;
		    											buffWrite.append("\t" + inter.getProperties().get(0).getName().toString() + "\n");
		    											TSequenceFlow seqFlow = (TSequenceFlow) flows.get(inter.getOutgoings().get(0).toString());
		    											if (seqFlow != null) {
		    												TBaseElement seqBase = (TBaseElement) seqFlow.getTargetRef();
		    												if (seqBase instanceof TScriptTask) {
		    													TScriptTask scriptIn = (TScriptTask) seqBase;
		    			    									buffWrite.append("\t" + scriptIn.getScript().getContent().get(0).toString() + "\n");
		    			    									
		    			    									TSequenceFlow flow = (TSequenceFlow) flows.get(scriptIn.getOutgoings().get(0).toString());
		    			    									if (flow != null) {
		    			    										TBaseElement seqBaseIn = (TBaseElement) flow.getTargetRef();
				    												if (seqBaseIn instanceof TEndEvent) {
				    													TEndEvent end = (TEndEvent) seqBaseIn;
						    											buffWrite.append("end");
				    												}
		    			    									}
		    												}
		    											}
		    										}
		    									}
		    								}
		    							}
    								}
		    						
		    						if (target instanceof TComplexGateway) {
			    						TComplexGateway complex = (TComplexGateway) target;
			    						buffWrite.append("\t" + "par do" + "\n");
			    						for (int r = 0; r < complex.getOutgoings().size(); r++) {
			    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(complex.getOutgoings().get(r).toString());
			    							if (seqOut != null) {
			    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
			    								System.out.println(out.getId());
			    							}
			    							
			    							if ((r+1) == complex.getOutgoings().size()) {
			    								buffWrite.append("\t" + "end" + "\n");	
			    							} else {
			    								buffWrite.append("\t" + "with" + "\n");	
			    							}
			    						}
		    						}
		    						if (target instanceof TExclusiveGateway) {
		    							TExclusiveGateway exclu = (TExclusiveGateway) target;
		    							buffWrite.append("\t" + "par/or do" + "\n");
			    						for (int r = 0; r < exclu.getOutgoings().size(); r++) {
			    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(exclu.getOutgoings().get(r).toString());
			    							if (seqOut != null) {
			    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
			    								System.out.println(out.getId());
			    							}
			    						}
		    						}
		    						if (target instanceof TParallelGateway) {
		    							TParallelGateway para = (TParallelGateway) target;
		    							buffWrite.append("\t" + "par/and do" + "\n");
			    						for (int r = 0; r < para.getOutgoings().size(); r++) {
			    							TSequenceFlow seqOut = (TSequenceFlow) flows.get(para.getOutgoings().get(r).toString());
			    							if (seqOut != null) {
			    								TBaseElement out = (TBaseElement) seqOut.getTargetRef();
			    								TScriptTask task = (TScriptTask) out;
			    								buffWrite.append("\t\t" + task.getScript().getContent().get(0).toString() + "\n");
			    								
			    								if (task.getOutgoings().size() != 0) {
			    									TSequenceFlow next = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
			    									if (next != null) {
			    										TBaseElement eleNext = (TBaseElement) next.getTargetRef();
			    										TIntermediateCatchEvent interCatch = (TIntermediateCatchEvent) eleNext;
			    										buffWrite.append("\t\t" + interCatch.getProperties().get(0).getName().toString() + "\n");
			    										
			    										if (interCatch.getOutgoings().size() != 0) {
			    											TSequenceFlow nextTo = (TSequenceFlow) flows.get(interCatch.getOutgoings().get(0).toString());
			    											if (nextTo != null) {
			    												TBaseElement end = (TBaseElement) nextTo.getTargetRef();
			    												if (end instanceof TParallelGateway) {
			    													saida = end;
			    												}
			    											}
			    										}
			    									}
			    								}
			    								
			    							}
			    							if ((r+1) == para.getOutgoings().size()) {
			    								buffWrite.append("\t" + "end" + "\n");	
			    							} else {
			    								buffWrite.append("\t" + "with" + "\n");	
			    							}
			    						}
		    						}
		    						
		    						// EXEMPLO 3
		    						if (target instanceof TScriptTask) {
		    							TScriptTask task = (TScriptTask) target;
		    							TSequenceFlow prox = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
		    							if (prox != null) {
		    								TBaseElement inclu = (TBaseElement) prox.getTargetRef();
		    								if (inclu instanceof TInclusiveGateway) {
		    									buffWrite.append("\t" + task.getScript().getContent().get(0).toString() + "\n");
	    										TInclusiveGateway inclusive = (TInclusiveGateway) inclu;
	    										for (int fi = 0; fi < inclusive.getOutgoings().size(); fi++) {
			    									TSequenceFlow nextTo = (TSequenceFlow) flows.get(inclusive.getOutgoings().get(fi).toString());
			    									if (nextTo != null) {
					    								TBaseElement hasInclusive = (TBaseElement) nextTo.getTargetRef();
					    								if (hasInclusive instanceof TInclusiveGateway) {
					    									TInclusiveGateway notElse = (TInclusiveGateway) hasInclusive;
					    									saidaIF = notElse;
					    									notIfElse = false;
				    									}
					    								if (hasInclusive instanceof TIntermediateCatchEvent) {
					    									TIntermediateCatchEvent event = (TIntermediateCatchEvent) hasInclusive;
						    								TTimerEventDefinition timer =  (TTimerEventDefinition) event.getEventDefinitions().get(0).getValue();
						    								buffWrite.append("\t\t" + "await " + timer.getTimeDuration().getContent().get(0).toString() + "; \n");
						    								escape = event;
					    								}
					    								if (notIfElse) {
					    									buffWrite.append("\t" + "else" + "\n");
					    									notIfElse = false;
					    								}
			    									}
												}
	    										buffWrite.append("\t" + "end" + "\n");
		    								}
		    							}
		    						}
		    						
		    						// EXEMPLO 3
		    						if (escape != null) {
		    							TIntermediateCatchEvent gate = (TIntermediateCatchEvent) escape;
		    							TSequenceFlow next = (TSequenceFlow) flows.get(gate.getOutgoings().get(0).toString());
		    							TGateway eleNext = (TGateway) next.getTargetRef();
		    							TSequenceFlow to = (TSequenceFlow) flows.get(eleNext.getOutgoings().get(0).toString());
		    							if (to != null) {
		    								TScriptTask script = (TScriptTask) to.getTargetRef();
		    								buffWrite.append("\t" + script.getScript().getContent().get(0).toString() + "\n");
		    								TSequenceFlow prox = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    								TIntermediateCatchEvent inter = (TIntermediateCatchEvent) prox.getTargetRef();
		    								buffWrite.append("\t" + inter.getProperties().get(0).getName().toString() + "\n");
			    							TSequenceFlow flow = (TSequenceFlow) flows.get(inter.getOutgoings().get(0).toString());
			    							TScriptTask task = (TScriptTask) flow.getTargetRef();
			    							buffWrite.append("\t" + task.getScript().getContent().get(0).toString() + "\n");
		    								
			    							TSequenceFlow ifFlow = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
			    							if (ifFlow != null) {
			    								TInclusiveGateway inclusive = (TInclusiveGateway) ifFlow.getTargetRef();
	    										for (int fi = 0; fi < inclusive.getOutgoings().size(); fi++) {
			    									TSequenceFlow nextTo = (TSequenceFlow) flows.get(inclusive.getOutgoings().get(fi).toString());
			    									if (nextTo != null) {
					    								TBaseElement hasInclusive = (TBaseElement) nextTo.getTargetRef();
					    								if (hasInclusive instanceof TInclusiveGateway) {
					    									TInclusiveGateway notElse = (TInclusiveGateway) hasInclusive;
					    									saidaIF = notElse;
					    									notIfExem3 = false;
				    									}
					    								if (hasInclusive instanceof TScriptTask) {
					    									TScriptTask scriptTask = (TScriptTask) hasInclusive;
					    									buffWrite.append("\t\t" + scriptTask.getScript().getContent().get(0).toString() + "\n");
					    									escape = (TInclusiveGateway) flows.get(scriptTask.getOutgoings().get(0).toString()).getTargetRef();
					    								}
					    								if (notIfExem3) {
					    									buffWrite.append("\t" + "else" + "\n");
					    									notIfExem3 = false;
					    								}
			    									}
												}
	    										buffWrite.append("\t" + "end" + "\n");
			    							}
		    							}
		    							TGateway inclu = (TGateway) escape;
		    							TSequenceFlow flow = (TSequenceFlow) flows.get(inclu.getOutgoings().get(0).toString());
	    								TScriptTask scriptTask = (TScriptTask) flow.getTargetRef();
	    								buffWrite.append("\t" + scriptTask.getScript().getContent().get(0).toString() + "\n");
	    								
	    								TSequenceFlow fim = (TSequenceFlow) flows.get(scriptTask.getOutgoings().get(0).toString());
	    								if (fim.getTargetRef() instanceof TEndEvent) {
	    									buffWrite.append("end");
										}
		    						}
		    						
		    						if (saida != null) {
		    							TGateway gate = (TGateway) saida;
		    							TSequenceFlow next = (TSequenceFlow) flows.get(gate.getOutgoings().get(0).toString());
		    							if (next != null) {
		    								TBaseElement eleNext = (TBaseElement) next.getTargetRef();
		    								TScriptTask task = (TScriptTask) eleNext;
		    								TSequenceFlow prox = (TSequenceFlow) flows.get(task.getOutgoings().get(0).toString());
		    								if (prox != null) {
		    									TBaseElement inclu = (TBaseElement) prox.getTargetRef();
		    									if (inclu instanceof TInclusiveGateway) {
		    										buffWrite.append("\t" + task.getScript().getContent().get(0).toString() + "\n");
		    										TInclusiveGateway inclusive = (TInclusiveGateway) inclu;
		    										for (int fi = 0; fi < inclusive.getOutgoings().size(); fi++) {
				    									TSequenceFlow nextTo = (TSequenceFlow) flows.get(inclusive.getOutgoings().get(fi).toString());
				    									if (nextTo != null) {
						    								TBaseElement hasInclusive = (TBaseElement) nextTo.getTargetRef();
						    								if (hasInclusive instanceof TInclusiveGateway) {
						    									TInclusiveGateway notElse = (TInclusiveGateway) hasInclusive;
						    									saidaIF = notElse;
						    									notIfElse = false;
					    									}
						    								if (notIfElse) {
						    									buffWrite.append("\t" + "else" + "\n");
						    								}
						    								if (hasInclusive instanceof TScriptTask) {
						    									TScriptTask script = (TScriptTask) hasInclusive;
						    									buffWrite.append("\t\t" + script.getScript().getContent().get(0).toString() + "\n");
						    									TSequenceFlow flowInclu = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
						    									saidaIF = (TInclusiveGateway) flowInclu.getTargetRef();
						    								}
				    									}
													}
		    										buffWrite.append("\t" + "end" + "\n");
		    									}
		    								}
		    							}
		    							if (saidaIF != null) {
		    								TSequenceFlow prox = (TSequenceFlow) flows.get(saidaIF.getOutgoings().get(0).toString());
		    								if (prox != null) {
			    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
			    								if (eleNext instanceof TIntermediateCatchEvent) {
				    								TIntermediateCatchEvent event = (TIntermediateCatchEvent) eleNext;
				    								TTimerEventDefinition timer =  (TTimerEventDefinition) event.getEventDefinitions().get(0).getValue();
				    								buffWrite.append("\t" + "await " + timer.getTimeDuration().getContent().get(0).toString() + "; \n");
				    								saidaTimer = event;
			    								}
		    								}
		    							}
		    						}
		    						if (saidaTimer != null) {
		    							TIntermediateCatchEvent event = (TIntermediateCatchEvent) saidaTimer;
		    							TSequenceFlow prox = (TSequenceFlow) flows.get(event.getOutgoings().get(0).toString());
		    							if (prox != null) {
		    								TBaseElement eleNext = (TBaseElement) prox.getTargetRef();
		    								if (eleNext instanceof TScriptTask) {
		    									TScriptTask script = (TScriptTask) eleNext;
		    									buffWrite.append("\t" + script.getScript().getContent().get(0).toString() + "\n");
		    									
		    									TSequenceFlow next = (TSequenceFlow) flows.get(script.getOutgoings().get(0).toString());
		    									if (next != null) {
		    										TBaseElement fim = (TBaseElement) next.getTargetRef();
		    										if (fim instanceof TEndEvent) {
		    											TEndEvent end = (TEndEvent) fim;
		    											buffWrite.append("end");
		    										}
		    									}
		    								}
		    							}
		    						}
		    					}
							}
	    				}
	    				
	    				
	    				
	    				}
	    				
	    				
	    				// pega todos os sequences flows que saem do SUBPROCESSO
	    				for (int j = 0; j < sub.getOutgoings().size(); j++) {
	    					//System.out.println(sub.getOutgoings().get(j));
						}	
					}
	    			
	    			
	    			
	    			
	    			// pega todos os scripts tasks
	    			if (Element.getId().startsWith("ScriptTask")) {
	    				TScriptTask script = (TScriptTask) Element;
	    				//System.out.println("FLOW : " + Element.getId());
	    				
	    				// pega todos os IN flows do script event
	    				for (int j = 0; j < script.getIncomings().size(); j++) {
	    					//System.out.println("IN : " + script.getIncomings().get(j));
						}
	    				
	    				// pega todos os OUT flows do script event
	    				for (int j = 0; j < script.getOutgoings().size(); j++) {
	    					//System.out.println("OUT : " + script.getOutgoings().get(j));
						}
	    				
	    				// pega os SCRIPT
	    				for (int j = 0; j < script.getScript().getContent().size(); j++) {
	    					//System.out.println("SCRIPT :");
	    					//System.out.println(script.getScript().getContent().get(j));
						}
					}
	    			
	    			
	    			// pega todos os ends Events
	    			if (Element.getId().startsWith("EndEvent")) {
	    				TEndEvent end = (TEndEvent) Element;
	    				//System.out.println("FLOW : " + Element.getId());
	    				
	    				// pega todos os sequences flows do end event
	    				for (int j = 0; j < end.getIncomings().size(); j++) {
	    					//System.out.println(end.getIncomings().get(j));
						}
					}	
	    		}
			}
		}
    	
    	buffWrite.close();
    	
    	/*Generator gen = new Generator("Teste");
    	gen.fileGenerator("Teste");
    	
    	Generator gen2 = new Generator("");
    	gen2.fileGenerator("");/*
    	
    	// BPMN DIAGRAM
    	
    	// Exemplo de como pegar os elementos pelo DI
    	/*for (int i = 0; i < def.getBPMNDiagrams().size(); i++) {
    		BPMNDiagram diagram = (BPMNDiagram) def.getBPMNDiagrams().get(i);
    		
    		BPMNPlane plane = diagram.getBPMNPlane();
    		
    		for (int j = 0; j < plane.getDiagramElements().size(); j++) {
    			
    			JAXBElement element = plane.getDiagramElements().get(j);
    			
    			if (element.getDeclaredType().isAssignableFrom(BPMNShape.class)) {
    				BPMNShape shape = (BPMNShape) element.getValue();
    				System.out.println("Um elemento Shape do id: " + shape.getBpmnElement());
    			}
    			
    			if (element.getDeclaredType().isAssignableFrom(BPMNEdge.class)) {
    				BPMNEdge edge = (BPMNEdge) element.getValue();
    				System.out.println("Um elemento Edge do id: " + edge.getBpmnElement());
    			}
    			
    		}
		}*/  
    }
}
