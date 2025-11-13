package net.bichal.bplb.server.datapack;

import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatapackHandler {
    private static final String OBJECTIVE_X = "bplb_x";
    private static final String OBJECTIVE_Y = "bplb_y";
    private static final String OBJECTIVE_Z = "bplb_z";

    public static void sendDatapackPositions(List<ServerPlayerEntity> players) {
        if (players.isEmpty()) return;

        ServerPlayerEntity firstPlayer = players.getFirst();
        Scoreboard scoreboard = Objects.requireNonNull(firstPlayer.getServer()).getScoreboard();
        ScoreboardObjective objX = getOrCreateObjective(scoreboard, OBJECTIVE_X, "BPLB X");
        ScoreboardObjective objY = getOrCreateObjective(scoreboard, OBJECTIVE_Y, "BPLB Y");
        ScoreboardObjective objZ = getOrCreateObjective(scoreboard, OBJECTIVE_Z, "BPLB Z");

        if (objX == null || objY == null || objZ == null) {
            Logger.error("Failed to create datapack objectives");
            return;
        }

        for (ServerPlayerEntity viewer : players) {
            List<PositionUpdatePayload.PositionData> positions = new ArrayList<>();

            for (ServerPlayerEntity target : players) {
                if (target.equals(viewer)) continue;

                try {
                    // Usar ScoreHolder en lugar de String
                    ScoreAccess xScore = scoreboard.getOrCreateScore(target, objX, false);
                    ScoreAccess yScore = scoreboard.getOrCreateScore(target, objY, false);
                    ScoreAccess zScore = scoreboard.getOrCreateScore(target, objZ, false);

                    if (xScore == null || yScore == null || zScore == null) continue;

                    int x = xScore.getScore();
                    int y = yScore.getScore();
                    int z = zScore.getScore();

                    positions.add(new PositionUpdatePayload.PositionData(
                            target.getUuid(),
                            x / 10.0,
                            y / 10.0,
                            z / 10.0,
                            0.0
                    ));
                } catch (Exception e) {
                    Logger.debug("Error reading scores for player {}: {}",
                            target.getName().getString(), e.getMessage());
                }
            }

            if (!positions.isEmpty() && ServerPlayNetworking.canSend(viewer, PositionUpdatePayload.ID)) {
                ServerPlayNetworking.send(viewer, new PositionUpdatePayload(
                        List.of(),
                        positions,
                        List.of()
                ));
            }
        }
    }

    private static ScoreboardObjective getOrCreateObjective(
            Scoreboard scoreboard,
            String name,
            String displayName
    ) {
        ScoreboardObjective existing = scoreboard.getNullableObjective(name);
        if (existing != null) {
            return existing;
        }

        try {
            // Crear objetivo con los 6 parámetros requeridos:
            // 1. name (String)
            // 2. criterion (ScoreboardCriterion)
            // 3. displayName (Text)
            // 4. renderType (RenderType)
            // 5. displayAutoUpdate (boolean)
            // 6. numberFormat (NumberFormat)
            return scoreboard.addObjective(
                    name,
                    ScoreboardCriterion.DUMMY,
                    Text.literal(displayName),
                    ScoreboardCriterion.RenderType.INTEGER,
                    false,
                    BlankNumberFormat.INSTANCE
            );
        } catch (Exception e) {
            Logger.error("Failed to create objective {}: {}", name, e.getMessage());
            return null;
        }
    }
}
