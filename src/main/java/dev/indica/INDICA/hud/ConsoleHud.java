package dev.indica.INDICA.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ConsoleHud extends HudElement {
    public static final HudElementInfo<ConsoleHud> INFO = new HudElementInfo<>(
        dev.indica.INDICA.INDICA.HUD_GROUP,
        "console-hud",
        "Displays system/console messages in a dedicated HUD.",
        ConsoleHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");

    private final Setting<Integer> maxLines = sgGeneral.add(new IntSetting.Builder()
        .name("max-lines")
        .description("Number of lines to display.")
        .defaultValue(12)
        .range(1, 200)
        .sliderRange(5, 50)
        .build()
    );

    private final Setting<Boolean> timestamps = sgGeneral.add(new BoolSetting.Builder()
        .name("timestamps")
        .description("Show timestamps.")
        .defaultValue(true)
        .build()
    );

    // Filters
    private final Setting<Boolean> showMeteor = sgGeneral.add(new BoolSetting.Builder()
        .name("show-meteor")
        .description("Show [Meteor] messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showSystem = sgGeneral.add(new BoolSetting.Builder()
        .name("show-system")
        .description("Show system/console messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showChatTag = sgGeneral.add(new BoolSetting.Builder()
        .name("show-chat-tag")
        .description("Show [CHAT] messages (only if system is shown).")
        .defaultValue(false)
        .visible(() -> showSystem.get())
        .build()
    );

    private final Setting<java.util.List<String>> customWhitelist = sgWhitelist.add(new StringListSetting.Builder()
        .name("custom-whitelist")
        .description("Show messages that contain any of these substrings.")
        .defaultValue(java.util.List.of())
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor(240, 240, 240, 255))
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color.")
        .defaultValue(new SettingColor(0, 0, 0, 90))
        .build()
    );

    private final Setting<Integer> maxWidth = sgGeneral.add(new IntSetting.Builder()
        .name("max-width")
        .description("Maximum width in pixels before wrapping. Set 0 for unlimited.")
        .defaultValue(220)
        .range(0, 1000)
        .sliderRange(100, 500)
        .build()
    );

    public ConsoleHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        List<ConsoleLine> lines = ConsoleBuffer.getSnapshot(Integer.MAX_VALUE);

        // Apply filters to original lines
        java.util.List<ConsoleLine> filtered = new java.util.ArrayList<>();
        for (ConsoleLine line : lines) {
            if (shouldShow(line.text)) filtered.add(line);
        }

        // Wrap into rendered lines for all filtered entries
        java.util.List<java.util.List<Segment>> renderedAll = new java.util.ArrayList<>();
        int mw = maxWidth.get();
        for (ConsoleLine line : filtered) {
            String s = formatLine(line);
            java.util.List<Segment> segs = parseColoredSegments(s, new Color(textColor.get()));
            if (mw <= 0) renderedAll.add(segs);
            else renderedAll.addAll(wrapSegments(segs, renderer, mw));
        }

        // Select only the last N displayed (wrapped) lines
        int toShow = Math.min(maxLines.get(), renderedAll.size());
        java.util.List<java.util.List<Segment>> renderedLines = renderedAll.subList(Math.max(0, renderedAll.size() - toShow), renderedAll.size());

        double width = 0;
        double lineHeight = renderer.textHeight(true);
        for (java.util.List<Segment> segLine : renderedLines) {
            String visible = segmentsVisibleText(segLine);
            double w = renderer.textWidth(visible, true);
            if (mw > 0) w = Math.min(w, mw);
            width = Math.max(width, w);
        }

        double height = lineHeight * renderedLines.size();

        // In HUD editor, always reserve box for configured size even if empty/hidden
        if (isInEditor()) {
            double editorWidth = maxWidth.get() > 0 ? Math.min(width, maxWidth.get()) : width;
            if (editorWidth == 0) editorWidth = Math.max(renderer.textWidth(" ", true), maxWidth.get() > 0 ? maxWidth.get() : 120);
            double editorHeight = Math.max(height, lineHeight * Math.max(1, maxLines.get()));
            setSize(editorWidth + 6, editorHeight + 6);
        } else {
            setSize(width + 6, height + 6);
        }

        renderer.quad(x, y, getWidth(), getHeight(), new Color(backgroundColor.get()));

        double ty = y + 3;
        for (java.util.List<Segment> segLine : renderedLines) {
            double tx = x + 3;
            for (Segment seg : segLine) {
                if (seg.text.isEmpty()) continue;
                renderer.text(seg.text, tx, ty, seg.color, true);
                tx += renderer.textWidth(seg.text, true);
            }
            ty += lineHeight;
        }
    }

    @Override
    public meteordevelopment.meteorclient.gui.widgets.WWidget getWidget(meteordevelopment.meteorclient.gui.GuiTheme theme) {
        meteordevelopment.meteorclient.gui.widgets.containers.WTable root = theme.table();

        // Top bar with Clear button
        meteordevelopment.meteorclient.gui.widgets.pressable.WButton clear = theme.button("Clear log");
        clear.action = () -> ConsoleBuffer.clear();
        root.add(clear).expandX();
        return root;
    }

    private String formatLine(ConsoleLine line) {
        if (!timestamps.get()) return line.text;
        return "[" + line.time + "] " + line.text;
    }

    public static void registerListener() {
        MeteorClient.EVENT_BUS.subscribe(Events.INSTANCE);
        installConsoleLogger();
    }

    public static class Events {
        public static final Events INSTANCE = new Events();
        private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

        @EventHandler
        private void onReceiveMessage(ReceiveMessageEvent event) {
            Text msg = event.getMessage();
            if (msg == null) return;
            if (ConsoleHud.isDeathMessage(msg)) return;
            String plain = msg.getString();
            boolean likelyPlayerChat = isLikelyPlayerChat(plain);
            if (likelyPlayerChat) return; // always hide player chat from console HUD
            ConsoleBuffer.add(new ConsoleLine(plain, LocalTime.now().format(TIME_FMT), false));
        }

        private boolean isLikelyPlayerChat(String s) {
            if (s == null || s.isEmpty()) return false;
            if (s.charAt(0) == '<') {
                int idx = s.indexOf('>');
                return idx > 1 && idx < 20;
            }
            return false;
        }


    }

    private record ConsoleLine(String text, String time, boolean playerChat) {}

    private static class ConsoleBuffer {
        private static final Deque<ConsoleLine> LINES = new ArrayDeque<>();
        private static final int HARD_LIMIT = 100;

        static synchronized void add(ConsoleLine line) {
            LINES.addFirst(line);
            while (LINES.size() > HARD_LIMIT) LINES.removeLast();
        }

        static synchronized List<ConsoleLine> getSnapshot(int limit) {
            List<ConsoleLine> out = new ArrayList<>(Math.min(limit, LINES.size()));
            for (ConsoleLine line : LINES) {
                out.add(line);
                if (out.size() >= limit) break;
            }
            return out.reversed();
        }

        static synchronized void clear() {
            LINES.clear();
        }
    }

    private boolean shouldShow(String text) {
        if (text == null) return false;
        String s = text;
        boolean hasMeteor = s.contains("[Meteor]");
        boolean hasChatTag = s.contains("[CHAT]");
        boolean hasSystem = s.contains("[System]") || hasChatTag;

        // Custom filters: if any matches, show regardless of other toggles
        String t = s.toLowerCase();
        for (String entry : customWhitelist.get()) {
            if (entry == null) continue;
            String e = entry.toLowerCase();
            if (!e.isEmpty() && t.contains(e)) return true;
        }

        if (hasMeteor) return showMeteor.get();
        if (hasChatTag) return showSystem.get() && showChatTag.get();
        if (hasSystem) return showSystem.get();

        // Treat untagged console/log lines as system
        return showSystem.get();
    }

    // Color parsing and wrapping helpers
    private static class Segment {
        final String text;
        final Color color;
        Segment(String text, Color color) { this.text = text; this.color = color; }
    }

    private static String segmentsVisibleText(List<Segment> segments) {
        StringBuilder sb = new StringBuilder();
        for (Segment s : segments) sb.append(s.text);
        return sb.toString();
    }

    private static List<Segment> parseColoredSegments(String raw, Color defaultColor) {
        List<Segment> out = new ArrayList<>();
        Color current = defaultColor;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '§' && i + 1 < raw.length()) {
                if (sb.length() > 0) { out.add(new Segment(sb.toString(), current)); sb.setLength(0); }
                char code = Character.toLowerCase(raw.charAt(++i));
                Color mapped = mapMcColor(code, defaultColor);
                if (mapped != null) current = mapped;
                continue;
            }
            sb.append(c);
        }
        if (sb.length() > 0) out.add(new Segment(sb.toString(), current));
        return out;
    }

    private static Color mapMcColor(char code, Color fallback) {
        return switch (code) {
            case '0' -> new Color(0x00,0x00,0x00);
            case '1' -> new Color(0x00,0x00,0xaa);
            case '2' -> new Color(0x00,0xaa,0x00);
            case '3' -> new Color(0x00,0xaa,0xaa);
            case '4' -> new Color(0xaa,0x00,0x00);
            case '5' -> new Color(0xaa,0x00,0xaa);
            case '6' -> new Color(0xff,0xaa,0x00);
            case '7' -> new Color(0xaa,0xaa,0xaa);
            case '8' -> new Color(0x55,0x55,0x55);
            case '9' -> new Color(0x55,0x55,0xff);
            case 'a' -> new Color(0x55,0xff,0x55);
            case 'b' -> new Color(0x55,0xff,0xff);
            case 'c' -> new Color(0xff,0x55,0x55);
            case 'd' -> new Color(0xff,0x55,0xff);
            case 'e' -> new Color(0xff,0xff,0x55);
            case 'f' -> new Color(0xff,0xff,0xff);
            case 'r' -> fallback;
            default -> null;
        };
    }

    private static List<List<Segment>> wrapSegments(List<Segment> segs, HudRenderer renderer, int maxWidthPx) {
        List<List<Segment>> lines = new ArrayList<>();
        String visible = segmentsVisibleText(segs);
        if (visible.isEmpty()) { lines.add(segs); return lines; }

        int start = 0;
        while (start < visible.length()) {
            int end = start;
            int lastSpace = -1;
            while (end < visible.length()) {
                String slice = visible.substring(start, end + 1);
                double w = renderer.textWidth(slice, true);
                if (w > maxWidthPx) break;
                if (Character.isWhitespace(visible.charAt(end))) lastSpace = end;
                end++;
            }
            int lineEnd;
            if (end >= visible.length()) lineEnd = visible.length();
            else if (lastSpace >= start) lineEnd = lastSpace + 1;
            else lineEnd = Math.max(start + 1, end);

            lines.add(cutSegments(segs, start, lineEnd));
            start = lineEnd;
        }
        return lines;
    }

    private static List<Segment> cutSegments(List<Segment> segs, int startVisible, int endVisible) {
        List<Segment> out = new ArrayList<>();
        int idx = 0;
        for (Segment s : segs) {
            int segStart = idx;
            int segEnd = idx + s.text.length();
            if (segEnd <= startVisible) { idx = segEnd; continue; }
            if (segStart >= endVisible) break;
            int from = Math.max(0, startVisible - segStart);
            int to = Math.min(s.text.length(), endVisible - segStart);
            if (from < to) out.add(new Segment(s.text.substring(from, to), s.color));
            idx = segEnd;
        }
        return out;
    }

    private static boolean isDeathMessage(Text msg) {
        if (msg.getContent() instanceof TranslatableTextContent tc) {
            String key = tc.getKey();
            return key != null && key.startsWith("death.");
        }
        return false;
    }

    private static void installConsoleLogger() {
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration cfg = ctx.getConfiguration();
            Appender app = new AbstractAppender("INDICAConsoleHud", null, null, false, null) {
                @Override public void append(LogEvent event) {
                    String m = event.getMessage() != null ? event.getMessage().getFormattedMessage() : null;
                    if (m == null || m.isEmpty()) return;
                    ConsoleBuffer.add(new ConsoleLine(m, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), false));
                }
            };
            app.start();
            LoggerConfig root = cfg.getRootLogger();
            root.addAppender(app, null, null);
            ctx.updateLoggers();
        } catch (Throwable ignored) { }
    }

}


