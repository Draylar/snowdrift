package draylar.snowdrift.logic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class SnowTickEventHandler {

    private static final SnowIncrementer snowIncrementer = new SnowIncrementer();
    private static final SnowDecrementer snowDecrementer = new SnowDecrementer();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            if (SnowIncrementer.canIncrementSnow(world)) {
                List<Chunk> loadedChunks = getLoadedChunks(world);

                if (world.isRaining()) {
                    snowIncrementer.tickSnow(world, loadedChunks);
                } else if (SnowDecrementer.canDecrementSnow(world)) {
                    snowDecrementer.tickClear(world, loadedChunks);
                }
            }
        });
    }

    /**
     * Retrieves a list of all loaded Chunks in the given world.
     * Accomplished by iterating over all connected players and storing each chunk within the server render distance from them.
     *
     * @param world world to retrieve loaded chunks from
     * @return a list of loaded chunks in the given world
     */
    private static List<Chunk> getLoadedChunks(ServerWorld world) {
        ArrayList<Chunk> loadedChunks = new ArrayList<>();
        int renderDistance = world.getServer().getPlayerManager().getViewDistance();

        world.getPlayers().forEach(player -> {
            ChunkPos playerChunkPos = new ChunkPos(player.getBlockPos());
            Chunk chunk = world.getChunk(playerChunkPos.x, playerChunkPos.z);

            if (!loadedChunks.contains(chunk)) {
                loadedChunks.add(chunk);
            }

            for (int x = -renderDistance; x <= renderDistance; x++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    ChunkPos offsetChunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);
                    Chunk offsetChunk = world.getChunk(offsetChunkPos.x, offsetChunkPos.z);

                    if (!loadedChunks.contains(offsetChunk)) {
                        loadedChunks.add(offsetChunk);
                    }
                }
            }
        });

        return loadedChunks;
    }
}
