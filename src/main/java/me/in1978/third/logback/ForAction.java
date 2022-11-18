package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import me.in1978.third.logback.util.Events;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;

public class ForAction extends Action implements InPlayListener {
    public static final Stack<ScopedContext> scopes = new Stack<>();
    private List<SaxEvent> events;

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        this.events = new ArrayList<>();
        ic.addInPlayListener(this);

        scopes.push(new ScopedContext(scopes.isEmpty() ? null : scopes.peek()));
        ScopedContext scopedContext = scopes.peek();

        scopedContext.declareAndPutLocal(HiddenItrAction.ATTS_KEY, attributes);
    }

    @Override
    public void end(InterpretationContext ic, String qname) throws ActionException {
        ic.removeInPlayListener(this);

        ScopedContext scopedContext = scopes.peek();

        Attributes attributes = scopedContext.getLocal(HiddenItrAction.ATTS_KEY);
        Iterator<Object> itr;
        boolean foundItr = (itr = tryItrOf(attributes)) != null
                || (itr = tryItrFromTo(attributes)) != null;
        if (!foundItr) {
            addError("cannot build iterator");
            return;
        }

        List<SaxEvent> hiddenEvents = Arrays.asList(
                Events.mockStartEvent((StartEvent) events.get(0), "hiddenItr", new HashMap<>()),
                Events.mockEndEvent(events.get(events.size() - 1), "hiddenItr")
        );

        List<SaxEvent> subEvents = events.subList(1, events.size() - 1);
        List<SaxEvent> evs = new ArrayList<>();
        evs.addAll(subEvents);
        evs.addAll(hiddenEvents);

        scopedContext.declareAndPutLocal(HiddenItrAction.ITR_KEY, itr);
        scopedContext.declareAndPutLocal(HiddenItrAction.EVENTS_KEY, evs);

        ic.getJoranInterpreter().getEventPlayer().addEventsDynamically(hiddenEvents, 1);
    }

    private Iterator<Object> tryItrOf(Attributes attributes) {
        String of = attributes.getValue("of");
        Object inObj = null;

        if (StringUtils.hasLength(of)) {
            inObj = this.context.getObject(of);
            if (inObj == null) {
                addError("value not found:" + of);
                return null;
            }
        }

        String ofSpring = attributes.getValue("of-spring");
        if (StringUtils.hasLength(ofSpring)) {
            inObj = InjectSpringEnvAction.getValue(ofSpring);
            if (inObj == null) {
                addError("spring value not found:" + ofSpring);
                return null;
            }
        }

        if (inObj == null) {
            addError("of/of-spring not set");
            return null;
        }

        if (inObj instanceof Iterable) {
            return ((Iterable) inObj).iterator();
        } else if (inObj instanceof Object[]) {
            return Arrays.asList((Object[]) inObj).iterator();
        } else {
            addError("not a Iterable or Object[]:" + of + ", " + of.getClass().getName());
            return null;
        }
    }

    private Iterator<Object> tryItrFromTo(Attributes attributes) {
        Number from = getVarOrConst(attributes, "from"),
                to = getVarOrConst(attributes, "to"),
                step = getVarOrConst(attributes, "step");

        if (from == null || to == null) {
            return null;
        }

        if (step == null) step = 1;
        double step2 = step.doubleValue();

        return DoubleStream.generate(new DoubleSupplier() {
                    double d = from.doubleValue() - step2;

                    @Override
                    public double getAsDouble() {
                        return d += step2;
                    }
                })
                .takeWhile(d -> d < to.doubleValue())
                .mapToObj(Double::valueOf)
                .map(Object.class::cast)
                .iterator()
                ;
    }

    private Number getVarOrConst(Attributes attributes, String name) {
        String xname = attributes.getValue(name);
        if (xname != null) {
            return (Number) this.context.getObject(xname);
        }

        String s = attributes.getValue(name + "-const");
        return s == null ? null : Double.parseDouble(s);
    }


    @Override
    public void inPlay(SaxEvent event) {
        this.events.add(event);
    }
}
