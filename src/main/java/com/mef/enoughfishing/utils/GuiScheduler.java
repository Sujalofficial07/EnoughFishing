package com.mef.enoughfishing.utils;

import com.mef.enoughfishing.gui.GuiEnoughFishing;
import net.minecraft.client.Minecraft;

/**
 * Defers GUI opening by N client ticks after a command fires.
 *
 * WHY: /mef is typed in chat. Minecraft closes the chat GuiScreen on the
 * same tick the command is dispatched, so any synchronous or
 * addScheduledTask() call gets wiped out by the chat-close logic.
 * Waiting ≥ 3 ticks guarantees the chat screen is fully gone before
 * we call displayGuiScreen().
 *
 * Thread: tick() must only be called from the main Minecraft thread.
 */
public final class GuiScheduler {

    private static final int DELAY_TICKS = 3;

    /** -1 = idle, >0 = counting down. Volatile: read from render, write from tick. */
    private static volatile int countdown = -1;

    private GuiScheduler() {}

    /** Call this from the command handler. Safe from any thread. */
    public static void scheduleOpenGui() {
        countdown = DELAY_TICKS;
    }

    /**
     * Called every {@link net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent}.
     * Opens GuiEnoughFishing when the countdown hits zero, but only if no
     * other screen is currently open (safety guard).
     */
    public static void tick() {
        if (countdown <= 0) return;
        countdown--;
        if (countdown == 0) {
            countdown = -1;
            Minecraft mc = Minecraft.getMinecraft();
            // currentScreen == null → chat is closed and nothing else is open
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiEnoughFishing());
            }
        }
    }
}
