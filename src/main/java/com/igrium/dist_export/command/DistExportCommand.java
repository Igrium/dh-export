package com.igrium.dist_export.command;

import com.igrium.dist_export.DHExport;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DistExportCommand {
    private static final SimpleCommandExceptionType INVALID_PATH = new SimpleCommandExceptionType(Text.literal("Invalid path"));
    private static final SimpleCommandExceptionType FAILED_EXPORT = new SimpleCommandExceptionType(Text.literal("Failed to export obj. See console for details."));

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal("DistExport").then(
                literal("export").then(
                        argument("file", StringArgumentType.string()).executes(DistExportCommand::export)
                )
        ));
    }

    private static int export(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String exportPath = StringArgumentType.getString(context, "file");
        if (!exportPath.endsWith(".obj")) {
            exportPath = exportPath + ".obj";
        }

        Path path;
        try {
            path = Paths.get(exportPath).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw INVALID_PATH.create();
        }

        DHExport.getInstance().exportDHWorld(path, feedback -> {
            context.getSource().sendFeedback(Text.literal(feedback));
        }).whenCompleteAsync((r, e) -> {
            if (e != null) {
                context.getSource().sendError(Text.literal("Error exporting mesh. See console for details."));
                DHExport.LOGGER.error("Error exporting mesh.", e);
            } else {
                context.getSource().sendFeedback(Text.literal("Exported mesh to " + path.toAbsolutePath()));
            }

        });

        return 0;
    }

}
