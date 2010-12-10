/*
 * CREDITS
 *  __________
 *
 *  Team page: http://isatab.sf.net/
 *    - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 *    - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 *    - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 *    - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 *    - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 *  Contributors:
 *    - Manon Delahaye (ISA team trainee:  BII web services)
 *    - Richard Evans (ISA team trainee: rISAtab)
 *
 *  ______________________
 *  Contacts and Feedback:
 *  ______________________
 *
 *  Project overview: http://isatab.sourceforge.net/
 *
 *  To follow general discussion: isatab-devel@list.sourceforge.net
 *  To contact the developers: isatools@googlegroups.com
 *
 *  To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 *  To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *  __________
 *  License
 *  __________
 *
 *  This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 *  __________
 *  Sponsors
 *  __________
 *  This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in partby the EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.export.sra.templateutil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SRATemplateLoader loads SRA template files from the SRA-templates folder and
 * sends the XML back as a String to be injected into the SRA-XML to be exported.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Jun 4, 2010
 */
public class SRATemplateLoader {
	private static final String SRA_TEMPLATE_LOCATION = "/SRA-templates/";

	public String getSRAProcessingTemplate(SRATemplate template,
										   Map<SRAAttributes, String> userDefinedAttributes) throws FileNotFoundException {

		String sraTemplate = getStringFromInputStream(getClass().getResourceAsStream(SRA_TEMPLATE_LOCATION + template.toString()));
		System.out.println(sraTemplate);
		sraTemplate = processSRATemplate(sraTemplate, userDefinedAttributes);

		return sraTemplate;
	}

	private String getStringFromInputStream(InputStream inputStream) {
		StringBuilder fileBuilder = new StringBuilder();
		if (inputStream != null) {
			String line;
			boolean isCommentBlock = false;
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));

				while ((line = reader.readLine()) != null) {
					if (line.contains("<!--")) {
						isCommentBlock = true;
					}

					if (!isCommentBlock) {
						fileBuilder.append(line);
						fileBuilder.append("\n");
					}

					if (line.contains("-->")) {
						isCommentBlock = false;
					}
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return fileBuilder.toString();
	}

	private String processTag(String tag, Map<SRAAttributes, String> userDefinedAttributes) {
		// remove the spaces we don't want
		tag = tag.replaceAll("\\$|\\(|\\)", "");

		String defaultValue = "";

		if (tag.contains("|")) {
			String[] values = tag.split("\\|");
			defaultValue = values[0];
			tag = values[1];
		}

		System.out.println("tag: " + tag);

		for (SRAAttributes userDefinedAttribute : userDefinedAttributes.keySet()) {
			if (userDefinedAttribute.getAttribute().equals(tag)) {
				return userDefinedAttributes.get(userDefinedAttribute);
			}
		}

		return defaultValue;
	}

	/**
	 * Processes the template String and parses the tags (e.g. $(Base Space|sequence_space) to replace these with
	 * either user defined values or the default value contained within the tag (e.g. Base Space).
	 *
	 * @param template			  - SRA template string to process
	 * @param userDefinedAttributes - Map from SRA Attributes to user defined value for replacement in template String
	 * @return String depicting the modified XML String
	 */
	public String processSRATemplate(String template, Map<SRAAttributes, String> userDefinedAttributes) {
		// string will be in the format <<1>>-<<14>>. need to extract the numbers!

		if (template.contains("$(")) {
			// match patterns like $(Base caller|base_caller) so that these tags may be processed
			String pattern = "(\\$\\((\\w)+\\)|\\$\\((.)+\\))";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(template);


			while (m.find()) {
				int startIndex = m.start();
				int endIndex = m.end();
				// we get the number in between the << >> so we add 2 to the start index and subtract 2 from the end index
				String tag = template.substring(startIndex, endIndex - 1);

				String part1 = template.substring(0, startIndex);
				String part2 = template.substring(endIndex);

				template = part1 + processTag(tag, userDefinedAttributes) + part2;
				// recursively call this method since direct substitutions inside the first string will change it's
				// length and therefore destroy String index locations...
				return processSRATemplate(template, userDefinedAttributes);
			}
		}
		return template;
	}

	public static void main(String[] args) {
		SRATemplateLoader loader = new SRATemplateLoader();

		try {
			String sratemplate = loader.getSRAProcessingTemplate(SRATemplate.PROCESSING_SOLID_PAIRED, new HashMap<SRAAttributes, String>());
			System.out.println(sratemplate);
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}