package me.in1978.third.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import me.in1978.third.logback.util.ChainedMap;
import me.in1978.third.logback.util.EventHelper;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyLogsAction extends Action implements InPlayListener {
    private List<SaxEvent> events;

    public static final String TAG_RESOURCE = "resource", TAG_FILE = "file", TAG_VALUE = "value", TAG_OF = "of", TAG_OF_SPRING = "of-spring",
            TAG_LOG_HOME = "log-home",
            TAG_PATTERN = "pattern",
            TAG_LEVEL = "level",
            TAG_ROLLING_FOLDER = "rolling-folder",
            TAG_ADDITIVITY = "additivity",
            TAG_MAX_HISTORY = "max-history";

    private Attributes atts;


    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        this.events = new ArrayList<>();
        ic.addInPlayListener(this);

        atts = attributes;

//        this.eventHelper = new EventHelper("for", ChainedMap.ins(), )
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        ic.removeInPlayListener(this);

        StartEvent startEvent = (StartEvent) events.get(0);

        EventHelper.BranchNode rootNode = new EventHelper.BranchNode(
                "for",
                ChainedMap.<String, String>ins()
                        .set("name", "log")
                        .set(TAG_OF, atts.getValue(TAG_OF))
                        .set(TAG_OF_SPRING, atts.getValue(TAG_OF_SPRING))
                , startEvent);
        rootNode.createBranchSimple("echo", "init logger for ${log} => ${log.target}");
        EventHelper.BranchNode appenderNode = rootNode.createBranch("appender",
                ChainedMap.<String, String>ins()
                        .set("name", "${log}")
                        .set("class", "ch.qos.logback.core.rolling.RollingFileAppender")
        );
        String logHome = getAtt(TAG_LOG_HOME);
        String logLevel = getAtt(TAG_LEVEL, "INFO");
        appenderNode.createBranchSimple("file", logHome + "/${log.target}.log");
        appenderNode.createBranch("encoder", ChainedMap.<String, String>ins()
                        .set("class", "ch.qos.logback.classic.encoder.PatternLayoutEncoder")
                )
                .createBranchSimple("pattern", getAtt(TAG_PATTERN))
        ;

        EventHelper.BranchNode rollingPolicyNode = appenderNode.createBranch("rollingPolicy",
                ChainedMap.<String, String>ins()
                        .set("class", "ch.qos.logback.core.rolling.TimeBasedRollingPolicy")
        );
        rollingPolicyNode.createBranchSimple("FileNamePattern",
                String.format("%s/%s/${log.target}.log.gz", logHome, getAtt(TAG_ROLLING_FOLDER, "history/%d{yyyy-MM-dd}")));
        rollingPolicyNode.createBranchSimple("MaxHistory", getAtt(TAG_MAX_HISTORY, "365"));

        EventHelper.BranchNode filterNode = appenderNode.createBranch("filter", ChainedMap.<String, String>ins().set("class", "ch.qos.logback.classic.filter.LevelFilter"));
        filterNode.createBranchSimple("level", logLevel);
        filterNode.createBranchSimple("onMatch", "accept");

        rootNode.createBranch("logger",
                        ChainedMap.<String, String>ins()
                                .set("name", "${log}")
                                .set("level", logLevel)
                                .set("additivity", getAtt(TAG_ADDITIVITY, "false"))
                )
                .createBranch("appender-ref", ChainedMap.<String, String>ins().set("ref", "${log}"))
        ;


        List<SaxEvent> makeEvents = rootNode.makeEvents();

        ic.getJoranInterpreter().getEventPlayer().addEventsDynamically(makeEvents, 1);
    }

    private String getAtt(String name) {
        String value = atts.getValue(name);
        if (value == null) {
            String msg = "attribute not set:" + name;
            addError(msg);
            throw new IllegalArgumentException(msg);
        }
        return value;
    }

    private String getAtt(String name, String def) {

        return Optional.ofNullable(atts.getValue(name))
                .orElseGet(() -> {
                    addWarn(String.format("%s not set. use default value: %s", name, def));
                    return def;
                });
    }

    @Override
    public void inPlay(SaxEvent event) {
        this.events.add(event);
    }
}
