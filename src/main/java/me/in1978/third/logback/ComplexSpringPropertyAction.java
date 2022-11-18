package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

public class ComplexSpringPropertyAction extends Action {
    @Override
    public void begin(InterpretationContext ic, String qname, Attributes attributes) throws ActionException {
        String name = attributes.getValue(NAME_ATTRIBUTE);
        String source = attributes.getValue("source");
        if (OptionHelper.isEmpty(name) || OptionHelper.isEmpty(source)) {
            addError("The \"name\" and \"source\" attributes of <springProperty> must be set");
            return;
        }

        Object value = InjectSpringEnvAction.getValue(source);
        ic.getContext().putObject(name, value);
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

    }
}
