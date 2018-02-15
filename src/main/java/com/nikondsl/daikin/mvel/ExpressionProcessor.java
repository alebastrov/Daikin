package com.nikondsl.daikin.mvel;

import java.io.IOException;
import java.util.*;
import org.mvel2.*;
import java.io.Serializable;
import com.nikondsl.daikin.DaikinBase;
import lombok.*;
import com.nikondsl.daikin.wireless.*;
import com.nikondsl.daikin.*;

public class ExpressionProcessor {
	
	private static class Rule {
		@Getter
		@Setter
		private String expression;
		@Getter
		@Setter
		private DaikinBase action;
		
	}
	
	public static void main(String[] args) throws IOException {
		Rule rule =new Rule();
		rule.setExpression("OuterTemperature < -15 && Power");
		DaikinBase action = DaikinFactory.createWirelessDaikin("http://192.168.1.215", 80);
		action.readDaikinState();
		
		
		Map<String, Object> vars = new LinkedHashMap<String, Object>();
		vars.put("OuterTemperature", action.getOutsideTemperature());
		vars.put("InnerTemperature", action.getInsideTemperature());
		vars.put("Power", action.isOn());
		
		action.setOn(false);
		rule.setAction(action);
		
//		String expr="4 > OuterTemperature && !Power";
		Serializable compiled = MVEL.compileExpression(rule.getExpression());
		Boolean ret = (Boolean) MVEL.executeExpression(compiled, vars);
		
		if (ret) rule.getAction().updateDaikinState();
		
		System.err.println("ret="+ret);
	}
}
