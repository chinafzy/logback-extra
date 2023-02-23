package me.in1978.third.logback.util;

import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class EventHelper {

    public static abstract class Node {
        public abstract List<SaxEvent> makeEvents();
    }

    @Data
    @RequiredArgsConstructor
    public static class BranchNode extends Node {
        private final String name;
        private final Map<String, String> atts;

        private final StartEvent sample;

        private List<Node> children = new ArrayList<>();

        public BranchNode addChild(Node child) {
            this.children.add(child);
            return this;
        }

        public BranchNode createBranch(String name, Map<String, String> atts) {
            BranchNode ret = new BranchNode(name, atts, sample);
            addChild(ret);
            return ret;
        }

        public LeafNode createText(String text) {
            LeafNode ret = new LeafNode(text, sample);
            addChild(ret);
            return ret;
        }

        public BranchNode createBranchSimple(String name, String text) {
            BranchNode ret = createBranch(name, null);
            ret.createText(text);
            return ret;
        }

        @Override
        public List<SaxEvent> makeEvents() {
            List<SaxEvent> ret = new ArrayList<>();

            ret.add(Events.mockStartEvent(sample, name, atts));
            children.forEach(child -> ret.addAll(child.makeEvents()));
            ret.add(Events.mockEndEvent(sample, name));

            return ret;
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class LeafNode extends Node {
        final String text;
        final SaxEvent sample;

        public SaxEvent makeEvent() {
            return Events.mockBodyEvent(sample, text);
        }

        @Override
        public List<SaxEvent> makeEvents() {
            return List.of(makeEvent());
        }
    }


}
