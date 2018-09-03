package org.ow2.orchestra.jaxb.bpmn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ow2.orchestra.jaxb.bpmn.marshaller.ExpressionUnmarshallerListener;
import org.ow2.orchestra.jaxb.bpmn.marshaller.NamespacePrefixMapper;
import org.ow2.orchestra.util.Misc;
import org.xml.sax.SAXException;


public final class JaxbUtil {

  private static final String JAXB_CONTEXT =
    "org.ow2.orchestra.jaxb.bpmn:org.ow2.orchestra.jaxb.bpmn.bpmndi:org.ow2.orchestra.jaxb.bpmn.di:org.ow2.orchestra.jaxb.bpmn.dc";


  private JaxbUtil() {

  }

  public static void marshalBpmn(final Definitions definitions, final File outputFile) throws JAXBException, SAXException, IOException {
    final String bpmnAsString = JaxbUtil.marshalBpmnToString(definitions);

    Misc.write(bpmnAsString, outputFile);
  }

  public static String marshalBpmnToString(final Definitions definitions) throws JAXBException, SAXException {
    final JAXBContext jc = JAXBContext.newInstance(JaxbUtil.JAXB_CONTEXT);
    final Marshaller marshaller = jc.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

    marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper());

    final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(Definitions.class.getClassLoader().getResource("BPMN20.xsd"));
    marshaller.setSchema(schema);

    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal(definitions, stringWriter);

    return stringWriter.toString();
  }

  public static Definitions unmarshalBpmn(final byte[] content) throws JAXBException, SAXException {
    final JAXBContext jc = JAXBContext.newInstance(JaxbUtil.JAXB_CONTEXT);
    final Unmarshaller unmarshaller = jc.createUnmarshaller();

    final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(Definitions.class.getClassLoader().getResource("BPMN20.xsd"));
    unmarshaller.setSchema(schema);
    unmarshaller.setListener(new ExpressionUnmarshallerListener());
    return (Definitions) unmarshaller.unmarshal(new ByteArrayInputStream(content));
  }


  public static Definitions unmarshalBpmn(final URL url) throws JAXBException, SAXException, IOException {
    return JaxbUtil.unmarshalBpmn(Misc.getAllContentFrom(url));
  }

  public static Definitions unmarshalBpmn(final File file) throws JAXBException, SAXException, IOException {
    return JaxbUtil.unmarshalBpmn(Misc.getAllContentFrom(file));
  }


}
