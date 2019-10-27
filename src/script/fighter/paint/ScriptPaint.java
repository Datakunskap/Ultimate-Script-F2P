package script.fighter.paint;

import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.Stats;
import script.fighter.config.ProgressiveSet;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.OgressWrapper;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

public final class ScriptPaint implements RenderListener {

    private static final int BASE_X = 6;
    private static final int BASE_Y = 6;

    private static final int DEFAULT_WIDTH_INCR = 20;

    private static final int BASE_HEIGHT = 20;
    private static final int LINE_HEIGHT = 20;

    private static final Color FOREGROUND = Color.WHITE;
    private static final Color BACKGROUND = Color.BLACK;
    private static final Stroke STROKE = new BasicStroke(1.8f);
    private final DecimalFormat formatNumber = new DecimalFormat("#,###");

    private final Map<String, PaintStatistic> stats;

    private Color outline;

    public ScriptPaint(Fighter context) {
        stats = new LinkedHashMap<>();
        outline = new Color(240, 0, 73);

        stats.put("Ultimate Ogress", new PaintStatistic(true, () -> "v" + "115" + " by " + "DrScatman"));
        stats.put("Runtime", new PaintStatistic(() -> Fighter.getRuntime().toElapsedString()));
        stats.put("Status", new PaintStatistic(() -> {
            Node active = context.getActive();
            return active == null ? "None" : active.getClass().getSimpleName() + " -> " + active.status();
        }));
        stats.put("Progressive", new PaintStatistic(() -> {
            Progressive set = ProgressiveSet.getCurrent();
            return set == null ? "None" : set.getName();
        }));
        stats.put("Killed", new PaintStatistic(() -> {
            HashMap<String, Integer> killed = Stats.getKilled();
            if(killed.size() == 0) {
                return "Nothing";
            }
            StringBuilder builder = new StringBuilder();
            killed.forEach((s, integer) -> builder.append(s).append(": ").append(integer).append(", "));
            String build = builder.toString();
            return build.substring(0, build.length() - 2);
        }));
        stats.put("Targeting Me", new PaintStatistic(() -> String.valueOf(CombatStore.getTargetingMe().size())));
        /*stats.put("Idle", new PaintStatistic(() -> {
           IdleNode node = (IdleNode) context.getSupplier().IDLE;
            String length = node.getIdleFor() + "s";
           if(node.getIdleFor() > 0) {
               return "For " + length;
           }
           return "In " + node.getKills() + " / " + node.getMax() + " Kills";
        }));*/
        stats.put("Items Alched", new PaintStatistic(()
                -> String.valueOf(OgressWrapper.itemsAlched)));
        stats.put("Deaths", new PaintStatistic(()
                -> String.valueOf(OgressWrapper.deaths)));
        stats.put("Inventory Value", new PaintStatistic(()
                -> formatNumber.format(BankWrapper.getInventoryValue())
                + (BankWrapper.isTradeRestricted() ? " (Trade Restricted)" : "")));
        stats.put("Total Value", new PaintStatistic(()
                -> formatNumber.format(BankWrapper.getTotalValue())
                + (BankWrapper.isTradeRestricted() ? " (Trade Restricted)" : "")));
        stats.put("Value Gained", new PaintStatistic(()
                -> formatNumber.format(BankWrapper.getTotalValueGained())
                + (BankWrapper.isTradeRestricted() ? " (Trade Restricted)" : "")));
        stats.put("Value / H", new PaintStatistic(()
                -> format((long) Fighter.getRuntime().getHourlyRate(BankWrapper.getTotalValueGained()))
                + (BankWrapper.isTradeRestricted() ? " (Trade Restricted)" : "")));
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public Color getOutline() {
        return outline;
    }

    public void setOutline(Color outline) {
        this.outline = outline;
    }

    public void submit(String key, PaintStatistic tracker) {
        stats.put(key, tracker);
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics2D g = (Graphics2D) e.getSource();
        Composite defaultComposite = g.getComposite();

        int width = 180;
        int currentX = BASE_X + (DEFAULT_WIDTH_INCR / 2);
        int currentY = BASE_Y + (LINE_HEIGHT / 2);

        g.setStroke(STROKE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(FOREGROUND);

        for (Map.Entry<String, PaintStatistic> entry : stats.entrySet()) {
            PaintStatistic stat = entry.getValue();
            String string = entry.getKey() + (stat.isHeading() ? " - " : ": ") + stat.toString();
            int currentWidth = g.getFontMetrics().stringWidth(string);
            if (currentWidth > width) {
                width = currentWidth;
            }
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.setColor(BACKGROUND);
        g.fillRoundRect(BASE_X, BASE_Y, width + DEFAULT_WIDTH_INCR, (stats.size() * LINE_HEIGHT) + BASE_HEIGHT, 7, 7);

        g.setComposite(defaultComposite);
        g.setColor(outline);
        g.drawRoundRect(BASE_X, BASE_Y, width + DEFAULT_WIDTH_INCR, (stats.size() * LINE_HEIGHT) + BASE_HEIGHT, 7, 7);

        g.setColor(FOREGROUND);
        for (Map.Entry<String, PaintStatistic> entry : stats.entrySet()) {
            PaintStatistic stat = entry.getValue();

            String string = entry.getKey() + (stat.isHeading() ? " - " : ": ") + stat.toString();
            int drawX = currentX;
            if (stat.isHeading()) {
                drawX = BASE_X + ((width + DEFAULT_WIDTH_INCR) - g.getFontMetrics().stringWidth(string)) / 2;
                g.setColor(outline);
                g.drawRect(BASE_X, currentY + (LINE_HEIGHT / 2) - BASE_Y + 1, width + DEFAULT_WIDTH_INCR, LINE_HEIGHT);

                g.setComposite(AlphaComposite.SrcOver.derive(0.1f));
                g.fillRect(BASE_X, currentY + (LINE_HEIGHT / 2) - BASE_Y + 1, width + DEFAULT_WIDTH_INCR, LINE_HEIGHT);
                g.setComposite(defaultComposite);

                g.setFont(g.getFont().deriveFont(Font.BOLD));
            } else {
                g.setFont(g.getFont().deriveFont(Font.PLAIN));
            }

            g.setColor(FOREGROUND);
            g.drawString(string, drawX, currentY += LINE_HEIGHT);
        }
    }
}
