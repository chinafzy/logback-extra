package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import me.in1978.third.logback.util.Utils;
import org.xml.sax.Attributes;

import java.util.List;

// TODO not work yet
public class ContinueAction extends Action {


    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        List<SaxEvent> allEvents = Utils.ognlGet(ic.getJoranInterpreter().getEventPlayer(), "events");
        int myIdx = Utils.indexOf(allEvents, se -> se instanceof StartEvent && ((StartEvent) se).getAttributes() == attributes);
        if (myIdx < 0) {
            throw new RuntimeException("what's up?");
        }

        List<SaxEvent> loopEvents = HiddenItrAction.getCurEvents();
        SaxEvent itrStart = loopEvents.get(loopEvents.size() - 2);
        int itrIdx = allEvents.lastIndexOf(itrStart);
        if (itrIdx < 0) {
            throw new RuntimeException("what's the matter?");
        }

        addInfo(String.format("%d events will be skipped.", itrIdx - myIdx - 2));
        for (int i = itrIdx - 1; i > myIdx + 1; i--) {
            allEvents.remove(i);
        }
    }


    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

    }
}
