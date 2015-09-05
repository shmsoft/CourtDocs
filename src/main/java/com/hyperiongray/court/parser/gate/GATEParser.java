package com.hyperiongray.court.parser.gate;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hyperiongray.court.CommonConstants;
import com.hyperiongray.court.parser.DataKey;
import com.hyperiongray.court.parser.IParser;
import com.hyperiongray.court.parser.Stats;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.creole.SerialAnalyserController;

public class GATEParser implements IParser {
	public static final String PARSER_TYPE = "gate";
	private static final Logger logger = Logger.getLogger(GATEParser.class);
	
	private Stats stats = new Stats();
	
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMdd,yyyy");
    
	private SerialAnalyserController gatePipeline;
	
	private String gateHome;
	
	@Override public Stats getStats() {
		return stats;
	}

	public GATEParser(String gateHome) throws Exception {
		this.gateHome = gateHome;
		gatePipeline = initGatePipeline();
	}
	
	private SerialAnalyserController initGatePipeline() throws Exception {

		Gate.setGateHome(new File(gateHome));
		Gate.setPluginsHome(new File(gateHome, "plugins"));
		Gate.setUserConfigFile(new File(gateHome, "user-gate.xml"));
		File japeDirectory = new File(gateHome, "jape");
		
		Gate.init();
		
		Gate.getCreoleRegister().registerDirectories(new File(Gate.getPluginsHome(), "ANNIE").toURI().toURL());
		
		SerialAnalyserController pipeline = (SerialAnalyserController) gate.Factory.createResource("gate.creole.SerialAnalyserController");
		
		LanguageAnalyser tokeniser = (LanguageAnalyser)gate.Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
		
		pipeline.add(tokeniser);
		
		LanguageAnalyser mainGazetteer = (LanguageAnalyser) gate.Factory.createResource("gate.creole.gazetteer.DefaultGazetteer");
		
		pipeline.add(mainGazetteer);
		
		logger.info("Adding GATE jape grammars");
		
		for (File file : japeDirectory.listFiles()) {
			if (file.isFile()) {
				if (file.getName().endsWith(".jape")) {
					LanguageAnalyser jape = (LanguageAnalyser)gate.Factory.createResource("gate.creole.Transducer", gate.Utils.featureMap(
				              "grammarURL", new File(japeDirectory, file.getName()).toURI().toURL(),
				              "encoding", "UTF-8"));
					logger.info("Added jape grammar " + file.getName());
					pipeline.add(jape);
				} else {
					logger.info("Ignored file in jape directory " + file.getName());
				}
			}
		}

		logger.info("GATE initialized");
		
		return pipeline;
	}

	@Override public Map<DataKey, String> parseFile(File file) throws Exception {
		
		Corpus corpus = gate.Factory.newCorpus(null);

		Map<DataKey, String> ret = new HashMap<DataKey, String>();
		
		Document doc = gate.Factory.newDocument(file.toURI().toURL());
		DocumentContent dc = doc.getContent();
		
		try {
			
			corpus.add(doc);
			
			gatePipeline.setCorpus(corpus);
			gatePipeline.execute();
	
			AnnotationSet annotations = doc.getAnnotations();
	
			List<Annotation> sortedAnnotations = new ArrayList<Annotation>();
			sortedAnnotations.addAll(annotations);
			
			Collections.sort(sortedAnnotations, new Comparator<Annotation>() {
				@Override public int compare(Annotation o1, Annotation o2) {
					if (o1.getStartNode().getOffset().longValue() < o2.getStartNode().getOffset().longValue()) {
						return -1;
					} else if (o1.getStartNode().getOffset().longValue() > o2.getStartNode().getOffset().longValue()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			
			Map<String, List<String>> annotationsMap = new HashMap<String, List<String>>();
			
			for (Annotation annotation : sortedAnnotations) {
				String type = annotation.getType();
				long startOffset = annotation.getStartNode().getOffset().longValue();
				long endOffset = annotation.getEndNode().getOffset().longValue();
				String value = dc.getContent(startOffset, endOffset).toString();
				if (!annotationsMap.containsKey(type)) {
					annotationsMap.put(type, new ArrayList<String>());
				}
				annotationsMap.get(type).add(value);
			}
	
			for (DataKey field : DataKey.values()) {
				switch (field) {
					case CaseName:
						ret.put(DataKey.CaseName, firstOrNull(GateAnnotation.CASE_NAME, annotationsMap));
						break;
					case File:
						ret.put(DataKey.File, file.getName());
						break;
					case Casenumber:
						ret.put(DataKey.Casenumber, firstOrNull(GateAnnotation.CASE_NUMBER_TYPE_2, annotationsMap));
						break;
					case CivilKriminal:
						String caseName = firstOrNull(GateAnnotation.CASE_NAME, annotationsMap);
						boolean isCriminal = false;
						if (!StringUtils.isEmpty(caseName)) {
							if (caseName.startsWith("People v")) {
								isCriminal = true;
							}
						}
						if (isCriminal) {
							stats.inc(DataKey.Criminal);
						} else {
							stats.inc(DataKey.Civil);
						}
						ret.put(DataKey.CivilKriminal, isCriminal? "K" : "C");
						break;
					case Court:
						ret.put(DataKey.Court, firstOrNull(GateAnnotation.US_COURT_TYPE, annotationsMap));
						break;
					case County:
						ret.put(DataKey.County, firstOrNull(GateAnnotation.US_COUNTY, annotationsMap));
						break;
					case Judge:
						ret.put(DataKey.Judge, listAll(GateAnnotation.JUDGE, annotationsMap));
						break;
					case DistrictAttorney:
						ret.put(DataKey.DistrictAttorney, firstOrNull(GateAnnotation.DISTRICT_ATTORNEY, annotationsMap));
						break;
					case ADA:
						ret.put(DataKey.ADA, firstOrNull(GateAnnotation.ADA, annotationsMap));
						break;
					case Keywords:
						ret.put(DataKey.Keywords, listAll(GateAnnotation.LEGAL_KEYWORD_IMPORTANT, annotationsMap));
						break;
					case GroundsForAppeal:
						ret.put(DataKey.GroundsForAppeal, listAll(GateAnnotation.GROUND_FOR_APPEAL_KEYWORD, annotationsMap));
						break;
					case Unanimous:
						boolean hasUnanimousKeyword = has(GateAnnotation.UNANIMOUS_KEYWORD, annotationsMap);
						ret.put(DataKey.Unanimous, hasUnanimousKeyword ? "1" : "0");
						break;
					case FirstDate:
						ret.put(DataKey.FirstDate, firstOrNull(GateAnnotation.DATE_OF_FILING, annotationsMap));
						break;
					case AppealDate:
						ret.put(DataKey.AppealDate, firstOrNull(GateAnnotation.APPEAL_DATE, annotationsMap));
						break;
					case Gap_days:
						Date firstDate = parseDate(firstOrNull(GateAnnotation.DATE_OF_FILING, annotationsMap));
						Date appealDate = parseDate(firstOrNull(GateAnnotation.APPEAL_DATE, annotationsMap));
						if (firstDate != null && appealDate != null) {
			                long gap = appealDate.getTime() - firstDate.getTime();
			                int gapDays = (int) (gap / 1000 / 60 / 60 / 24);
			                if (gapDays > 0) {
			                    ret.put(DataKey.Gap_days, Integer.toString(gapDays));
			                }
			            }
						break;
					case ModeOfConviction:
						ret.put(DataKey.ModeOfConviction, firstOrNull(GateAnnotation.MODE_OF_CONVICTION, annotationsMap));
						break;
					case Crimes:
						ret.put(DataKey.Crimes, firstOrNull(GateAnnotation.CRIME, annotationsMap));
						break;
					case Judges:
						ret.put(DataKey.Judges, listAll(GateAnnotation.WITNESSING_JUDGE, annotationsMap));
						break;
					case Defense:
						ret.put(DataKey.Defense, listAll(GateAnnotation.DEFENDER_TYPE, annotationsMap));
						break;
					case DefendantAppellant:
						boolean value = has(GateAnnotation.DEFENDANT_APPELLANT, annotationsMap);
						ret.put(DataKey.DefendantAppellant, value ? "1" : "0");
						break;
					case DefendantRespondent:
						boolean hasDefendantRespondent = has(GateAnnotation.DEFENDANT_RESPONDENT, annotationsMap);
						ret.put(DataKey.DefendantRespondent, hasDefendantRespondent ? "1" : "0");
						break;
					case HarmlessError:
						break;
					case ProsecutMisconduct:
						ret.put(DataKey.ProsecutMisconduct, firstOrNull(GateAnnotation.PROSECUT_MISCONDUCT, annotationsMap));
						break;
					case DocumentLength:
						ret.put(DataKey.DocumentLength, "0");
						break;
					default:
						if (field.isOutputToFile()) {
							logger.error("Unhandled output field " + field);
						}
				}
			}
			for (DataKey field : new HashMap<DataKey, String>(ret).keySet()) {
				String value = ret.get(field);
				value = sanitize(value);
				ret.remove(field);
				ret.put(field, value);
				stats.inc(field, value);
			}
			
	
			corpus.cleanup();
			gatePipeline.cleanup();
		
		} finally {
			corpus.clear();
			Factory.deleteResource(doc);
		}
		
		return ret;
		
	}
	
	private Date parseDate(String dateStr) {
		dateStr = sanitize(dateStr);
		try {
			return StringUtils.isEmpty(dateStr) ? null : dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.error("Unparseable date found " + dateStr);
			return null;
		}
	}

	private boolean has(GateAnnotation annotation, Map<String, List<String>> annotationsMap) {
		return annotationsMap.containsKey(annotation.getType()) && !annotationsMap.get(annotation.getType()).isEmpty();
	}

	private String sanitize(String value) {
	    value = value.replaceAll("\\" + CommonConstants.CSV_FIELD_SEPARATOR, "");
	    value = value.replaceAll("\\r\\n|\\r|\\n|\"", " ");
	    value = value.trim();
	    return value;
    }

	private String listAll(GateAnnotation annotation, Map<String, List<String>> annotationsMap) {
		if (!annotationsMap.containsKey(annotation.getType())) {
			return "";
		}
		List<String> values = annotationsMap.get(annotation.getType());
		Set<String> uniqueValues = new HashSet<String>();
		for (String value : values) {
			uniqueValues.add(sanitize(value));
		}
		StringBuilder ret = new StringBuilder();
		int cnt = 0;
		for (String value : uniqueValues) {
			ret.append(value);
			if (cnt < uniqueValues.size() - 1) {
				ret.append(";");
			}
			cnt++;
		}
		return ret.toString();
	}

	private String firstOrNull(GateAnnotation annotation, Map<String, List<String>> annotationsMap) {
		return annotationsMap.containsKey(annotation.getType()) ? annotationsMap.get(annotation.getType()).get(0) : "";
	}

}
