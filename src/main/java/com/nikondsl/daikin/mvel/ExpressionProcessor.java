package com.nikondsl.daikin.mvel;

import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.DaikinFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.mvel2.MVEL;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpressionProcessor {
	
	@ToString
	private static class Rule {
		@Getter
		@Setter
		private String expression;
		@Getter
		@Setter
		private DaikinBase action;
		
		@Getter
		@Setter
		private String nameOfRule;
		
	}
	
	public static void main(String[] args) throws Exception {
		
		loadFromConfig();
		
		if (true) return;
//		Rule rule =new Rule();
//		rule.setExpression("OuterTemperature < -1 && Power");
		Rule rule =new Rule();
		rule.setExpression("InnerTemperature <= 20 && !Power");
		DaikinBase action = DaikinFactory.createWirelessDaikin("http://192.168.1.215", 80);
		action.readDaikinState();
		
		
		Map<String, Object> vars = new LinkedHashMap<String, Object>();
		vars.put("OuterTemperature", action.getOutsideTemperature());
		vars.put("InnerTemperature", action.getInsideTemperature());
		vars.put("Power", action.isOn());
		
		action.setOn(true);
		rule.setAction(action);
		
//		String expr="4 > OuterTemperature && !Power";
		Serializable compiled = MVEL.compileExpression(rule.getExpression());
		Boolean ret = (Boolean) MVEL.executeExpression(compiled, vars);
		
		if (ret) rule.getAction().updateDaikinState();
		
		System.err.println("ret="+ret);
	}
	
	public static Document parse(File rulesFile) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(rulesFile);
		return document;
	}
	
	private static List<Rule> loadFromConfig() throws DocumentException {
		Path path = Paths.get("rules.xml").toAbsolutePath();
		Document rules = parse(path.toFile());
		List<Node> list = rules.selectNodes("//rule");
	
		for(Node node : list) {
			String name = ((DefaultElement) node).element("name").getText();
			String expression = ((DefaultElement) node).element("expression").getText();
			Node actionNode = node.selectSingleNode("action");
			String power = ((DefaultElement) actionNode).element("power").getText();
			Rule rule = new Rule();
			rule.setNameOfRule(name);
			rule.setExpression(expression);
			DaikinBase action = DaikinFactory.createWirelessDaikin("http:192.168.1.215", 80);
			action.setOn("on".equalsIgnoreCase(power));
			rule.setAction(action);
			System.err.println("=== "+ rule);
		}
		return null;
	}
}
