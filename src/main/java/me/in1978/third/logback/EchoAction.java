package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import org.xml.sax.Attributes;

public class EchoAction extends Action {
    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
    }

    @Override
    public void body(InterpretationContext ic, String body) throws ActionException {
        String s = ic.subst(body);
        System.out.println(s);
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
    }
}
