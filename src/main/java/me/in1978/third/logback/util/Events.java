package me.in1978.third.logback.util;

import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ElementPath;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Events {

    public static Attributes mockAttributes(Attributes atts, Map<String, String> local) {
        AttributesImpl ret = new AttributesImpl(atts);

        if (local != null) {
            local.entrySet().forEach(ent -> {
                String key = ent.getKey(), value = ent.getValue();
                int idx = ret.getIndex(key);
                if (idx != -1) {
                    ret.setValue(idx, value);
                } else {
                    ret.addAttribute("", key, key, "CDATA", value);
                }
            });
        }

        return ret;
    }

    public static StartEvent mockStartEvent(StartEvent event, String name, Map<String, String> values) {

        Class<StartEvent> clazz = StartEvent.class;
        Constructor<StartEvent> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(ElementPath.class, String.class, String.class, String.class, Attributes.class, Locator.class);
            constructor.setAccessible(true);
            return constructor.newInstance(
                    event.elementPath,
                    event.namespaceURI,
                    name,
                    name,
                    mockAttributes(event.attributes, values),
                    event.locator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EndEvent mockEndEvent(SaxEvent event, String name) {
        Class<EndEvent> clazz = EndEvent.class;
        Constructor<EndEvent> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(String.class, String.class, String.class, Locator.class);
            constructor.setAccessible(true);
            return constructor.newInstance(
                    event.namespaceURI,
                    name,
                    name,
                    event.locator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BodyEvent mockBodyEvent(SaxEvent event, String text) {
        Class<BodyEvent> clazz = BodyEvent.class;
        Constructor<BodyEvent> constructor;

        try {
            constructor = clazz.getDeclaredConstructor(String.class, Locator.class);
            constructor.setAccessible(true);
            return constructor.newInstance(text, event.getLocator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
