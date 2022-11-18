package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import me.in1978.third.logback.util.Utils;
import org.springframework.core.env.Environment;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InjectSpringEnvAction extends Action {

    public static Environment springEnv;

    public static Object getValue(String name) {
        Object ret = springEnv.getProperty(name, Object.class);
        if (ret != null) {
            return ret;
        }

        List<Object> list = new ArrayList<>();
        for (int i = 0; ; i++) {
            Object item = springEnv.getProperty(name + "[" + i + "]", Object.class);
            if (item == null) break;

            list.add(item);
        }

        return list.isEmpty() ? null : list;
    }

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) {

        Stack<List<Action>> actionListStack = Utils.ognlGet(ic, "joranInterpreter.actionListStack");
        Action springPropertyAction = actionListStack.stream()
                .flatMap(actions ->
                        actions.stream().filter(action -> action.getClass().getName().equals("org.springframework.boot.logging.logback.SpringPropertyAction"))
                )
                .findFirst()
                .orElse(null);
        if (springPropertyAction == null) {
            throw new RuntimeException("Put me inside SpringProperty.");
        }

        springEnv = Utils.ognlGet(springPropertyAction, "environment");
    }

    @Override
    public void end(InterpretationContext ic, String name) {
    }
}
