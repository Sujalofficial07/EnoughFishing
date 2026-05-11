package com.mef.enoughfishing.command;

import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.utils.GuiScheduler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * /mef  [gui | reset]
 *
 * GUI open is handled by GuiScheduler.scheduleOpenGui() which delays
 * by 3 ticks — enough for the chat screen to fully close first.
 */
public final class CommandMEF extends CommandBase {

    private static final List<String> SUB_CMDS = Arrays.asList("gui", "reset");

    @Override public String getCommandName()              { return "mef"; }
    @Override public int    getRequiredPermissionLevel()  { return 0; }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mef [gui | reset]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("enoughfishing");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String sub = (args.length > 0) ? args[0].toLowerCase() : "gui";
        switch (sub) {
            case "gui":
            case "":
                // Schedule GUI open AFTER the chat screen closes (3 ticks later).
                GuiScheduler.scheduleOpenGui();
                break;
            case "reset":
                FishingTracker.INSTANCE.resetStats();
                sender.addChatMessage(chat("§a[MEF] §fSession stats reset."));
                break;
            default:
                sender.addChatMessage(chat("§c[MEF] §fUsage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1
               ? getListOfStringsMatchingLastWord(args, SUB_CMDS)
               : Collections.emptyList();
    }

    private static ChatComponentText chat(String s) {
        return new ChatComponentText(s);
    }
}
