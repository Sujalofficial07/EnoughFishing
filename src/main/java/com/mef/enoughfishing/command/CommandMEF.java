package com.mef.enoughfishing.command;

import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.gui.GuiEnoughFishing;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client-side command {@code /mef} (alias: /enoughfishing).
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code /mef}       — opens the config GUI</li>
 *   <li>{@code /mef gui}   — same</li>
 *   <li>{@code /mef reset} — resets session statistics</li>
 * </ul>
 * </p>
 *
 * <p>GUI display is scheduled on the main thread via {@code addScheduledTask}
 * to prevent screen-switching from within an event callback, which would cause
 * a ConcurrentModificationException in Minecraft's GuiScreen list.</p>
 */
public final class CommandMEF extends CommandBase {

    private static final List<String> SUB_CMDS = Arrays.asList("gui", "reset");

    @Override public String getCommandName()  { return "mef"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mef [gui | reset]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("enoughfishing");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // client-side only, always allowed
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            // Must open GUI on the main thread after the command tick completes.
            Minecraft.getMinecraft().addScheduledTask(
                () -> Minecraft.getMinecraft().displayGuiScreen(new GuiEnoughFishing())
            );
        } else if (args[0].equalsIgnoreCase("reset")) {
            FishingTracker.INSTANCE.resetStats();
            sender.addChatMessage(info("Session stats reset."));
        } else {
            sender.addChatMessage(error("Unknown sub-command. Usage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, SUB_CMDS) : Collections.emptyList();
    }

    // ── Chat helpers ──────────────────────────────────────────────────────────

    private static ChatComponentText info(String msg)  {
        return new ChatComponentText("§b[MEF] §f" + msg);
    }
    private static ChatComponentText error(String msg) {
        return new ChatComponentText("§c[MEF] §f" + msg);
    }
}
