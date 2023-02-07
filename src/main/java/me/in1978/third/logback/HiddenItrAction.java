package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import lombok.RequiredArgsConstructor;
import me.in1978.third.logback.util.Events;
import org.xml.sax.Attributes;

import java.util.*;

@RequiredArgsConstructor
public class HiddenItrAction extends Action {

    public static final String
            ITR_KEY = "__hidden_itr__",
            ATTS_KEY = "__hidden_itr__attributes__",
            EVENTS_KEY = "__hidden_itr__events__";
    private static final Stack<ScopedContext> scopes = ForAction.scopes;

    public static List<SaxEvent> getCurEvents() {
        return scopes.peek().getLocal(EVENTS_KEY);
    }

    public static Attributes getCurAttributes() {
        return scopes.peek().getLocal(ATTS_KEY);
    }

    public static Iterator<Object> getCurItr() {
        return scopes.peek().getLocal(ITR_KEY);
    }

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        // nothing to do
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        final ScopedContext scopedContext = ForAction.scopes.peek();
        final Iterator<Object> itr = scopedContext.getLocal(ITR_KEY);

        // all done.
        if (!itr.hasNext()) {
            ForAction.scopes.pop();
            return;
        }

        final Attributes _atts = scopedContext.getLocal(ATTS_KEY);
        String qname = _atts.getValue("name");

        Object o = itr.next();
        LogDef logDef = parse(o);

        final List<SaxEvent> members = scopedContext.getLocal(EVENTS_KEY);

        List<SaxEvent> evs = new ArrayList<>();
        evs.addAll(proEvents(members, qname, logDef.getOri()));
        evs.addAll(proEvents(members, qname + ".target", logDef.getTarget()));
        evs.addAll(members);

        // next loop
        ic.getJoranInterpreter().getEventPlayer().addEventsDynamically(evs, 1);
    }

    private static List<SaxEvent> proEvents(List<SaxEvent> members, String name, String value) {
        Map<String, String> local = new HashMap<>();
        local.put("name", name);
        local.put("value", value);
        StartEvent propertyStart = Events.mockStartEvent((StartEvent) members.get(0), "property", local);
        EndEvent propertyEnd = Events.mockEndEvent(members.get(members.size() - 1), "property");
        return List.of(propertyStart, propertyEnd);
    }

    private static LogDef parse(Object v) {
        String s = "" + v;
        String[] arr = s.split(":");
        String ori = arr[0].trim();
        String target = (arr.length > 1 ? arr[1] : arr[0]).trim();
        return new LogDef(ori, target);
    }

}
