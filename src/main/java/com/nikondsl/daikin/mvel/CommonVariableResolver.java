package com.nikondsl.daikin.mvel;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class CommonVariableResolver {
    public static void main(String[] args) {
//        VariableResolverFactory factory = new CachedMapVariableResolverFactory();
//
//        VariableResolver outerTemperarture = factory.createVariable("OuterTemperature", Double.class);
//        VariableResolver innerTemperarture = factory.createVariable("InnerTemperature", Double.class);
//        Serializable compiledExpression = MVEL.compileExpression("OuterTemperature < 22.0", new ParserContext());
//        System.err.println("" + MVEL.executeExpression(compiledExpression));



        ParserContext context = new ParserContext();
        context.setStrictTypeEnforcement(true);
//        context.addImport(Ship.class);
//        context.addImport(MapObject.class);
//        context.addInput("obj", MapObject.class);
        Object compiled = MVEL.compileExpression("OuterTemperature < 22.0", context);
        Map<String, Object> vars = new HashMap<>();
        vars.put("OuterTemperature", 21.199);
        VariableResolverFactory varsResolver = new MapVariableResolverFactory(vars);
        System.err.println(MVEL.executeExpression(compiled, varsResolver));

    }
}
