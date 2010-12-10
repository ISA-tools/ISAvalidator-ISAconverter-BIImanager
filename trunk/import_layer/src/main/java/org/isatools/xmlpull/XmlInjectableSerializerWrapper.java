/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee: BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements: Êhttp://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

//package org.isatools.xmlpull;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.RandomAccessFile;
//import java.io.Reader;
//import java.io.Writer;
//
//import org.xmlpull.v1.XmlPullParserException;
//import org.xmlpull.v1.XmlSerializer;
//import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;
//import org.xmlpull.v1.wrapper.classic.StaticXmlSerializerWrapper;
//
///**
// * WARNING: this class is TODO: to be completed and tested. And also what we have here could go inside the PRIDE marshaller. 
// * date: May 27, 2008
// *
// * @author brandizi
// *
// */
//public class XmlInjectableSerializerWrapper extends StaticXmlSerializerWrapper
//{
//	protected Writer out = null;
//
//	public XmlInjectableSerializerWrapper ( XmlSerializer xs, XmlPullWrapperFactory wf ) 
//	{
//		super ( xs, wf );
//	}
//
//	
//  public void setOutput ( Writer writer ) throws IOException
//  {
//  	out = writer;
//  	xs.setOutput ( writer );
//  }
//  
//  
//  public void setOutput ( OutputStream os, String encoding ) throws IOException
//  {
//	  if ( os == null ) throw new IllegalArgumentException ( "output stream can not be null" );
//	
//	  if ( encoding != null ) {
//	    out = new OutputStreamWriter ( os, encoding );
//	  } else {
//      out = new OutputStreamWriter ( os );
//	  }
//	  
//	  setOutput ( out );
//  }
//
//
//  /**
//   * Injects the XML coming from xmlReader as-is into the output writer set up for this serializer
//   * This is useful when your target XML stream needs to embed another large XML document.  
//   *   
//   */
//  public XmlInjectableSerializerWrapper injectXml ( Reader xmlReader ) throws IOException, XmlPullParserException
//	{
//  	if ( out == null ) 
//  		throw new XmlPullParserException ( 
//  			"Sorry, XmlInjectableSerializerWrapper needs that you set the writer" +
//  			" or the output from its own methods, we cannot get the this from the embedded serializer" 
//  	);
//  	char rootch = seekRootElement ( xmlReader );
//  	out.write ( "<" + rootch );
//  	char [] buffer = new char [ 1024 ];
//  	while ( true ) 
//  	{
//	  	int nread = xmlReader.read ( buffer );
//	  	if ( nread == -1 ) break;
//	  	out.write ( buffer );
//  	}
//  	return this;
//	}
//
//
//  /** 
//   * Skips the prolog part and goes up to the first character of the root element. Returns the latter.
//   * Throws an exception in case no root element is present. We need to return the first character of the root element, 
//   * because we cannot go rewind the reader (and I don't like to use {@link RandomAccessFile}. 
//   * 
//   */
//  protected char seekRootElement ( Reader xmlReader ) throws IOException, XmlPullParserException 
//  {
//  	final int STATUS_PROLOG_EL_OPEN = 0, STATUS_READING = 1;
//  	
//  	int status = STATUS_READING;
//  	int line = 0;
//  	char c;
//  	
//  	do
//  	{
//    	c = (char) xmlReader.read ();
//  		
//    	switch ( c ) 
//  		{
//  			case '<':
//  				if ( status == STATUS_PROLOG_EL_OPEN )
//  					throw new XmlPullParserException ( "Syntax error in parsing the prolog section of XML at line" + line );
//  				
//  				c = (char) xmlReader.read ();
//  			  if ( c == '?' || c == '!' || c == '[' )
//  			  	status = STATUS_PROLOG_EL_OPEN;
//  			  else {
//  			  	// Return the character you've read
//  			  	return c;
//  			  }
//  			break;
//  			
//  			case '>': 
//  				if ( status == STATUS_PROLOG_EL_OPEN )
//  					status = STATUS_READING;
//  				else 
//  					throw new XmlPullParserException ( "Syntax error in parsing the prolog section of XML at line" + line );
//  				
//  			case '\n': line++;
//  		}
//  	}
//  	while ( c != (char) -1 );
//  	
//		throw new XmlPullParserException ( 
//			"Syntax error in parsing the XML, no root element found, line" + line  
//		);
//  
//  }
//
//}
