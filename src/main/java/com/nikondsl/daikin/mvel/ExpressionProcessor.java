package com.nikondsl.daikin.mvel;

import com.beust.jcommander.Strings;
import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.DaikinFactory;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpressionProcessor {
	private static final Logger LOG = LogManager.getLogger(ExpressionProcessor.class);
	
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
		
		List<Rule> rules = loadFromConfig();
		DaikinBase currentState = DaikinFactory.createWirelessDaikin("http://192.168.1.215", 80);
		for (Rule rule : rules) {
			currentState.readDaikinState();
			Map<String, Object> vars = new LinkedHashMap<String, Object>();
			vars.put("OuterTemperature", currentState.getOutsideTemperature());
			vars.put("InnerTemperature", currentState.getInsideTemperature());
			vars.put("Power", currentState.isOn());
			Serializable compiled = MVEL.compileExpression(rule.getExpression());
			Boolean ret = (Boolean) MVEL.executeExpression(compiled, vars);
			
			if (ret) {
				rule.getAction().updateDaikinState();
				LOG.info("Сработало правило; " + rule.getNameOfRule());
			}
		}
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
		List<Rule> result = new ArrayList<>();
	
		for(Node node : list) {
			String name = ((DefaultElement) node).element("name").getText();
			String expression = ((DefaultElement) node).element("expression").getText();
			Node actionNode = node.selectSingleNode("action");
			String power = ((DefaultElement) actionNode).element("power").getText();
			String mode =  ((DefaultElement) actionNode).element("mode").getText();
			Node fanNode = actionNode.selectSingleNode("fan");
			String fanSpeed =  ((DefaultElement) fanNode).element("speed").getText();
			String fanDirection =  ((DefaultElement) fanNode).element("direction").getText();
			Rule rule = new Rule();
			rule.setNameOfRule(name);
			rule.setExpression(expression);
			DaikinBase action = DaikinFactory.createWirelessDaikin("http://192.168.1.215", 80);
			action.setOn("on".equalsIgnoreCase(power));
			action.setMode(Mode.getParser().parseCommand(mode));
			if (!Strings.isStringEmpty(fanSpeed)) {
				action.setFan(Fan.getParser().parseCommand(fanSpeed));
			}
			if (!Strings.isStringEmpty(fanDirection)) {
				action.setFanDirection(FanDirection.getParser().parseCommand(fanDirection));
			}
			rule.setAction(action);
			result.add(rule);
			System.err.println("=== "+ rule);
		}
		return result;
	}
}
