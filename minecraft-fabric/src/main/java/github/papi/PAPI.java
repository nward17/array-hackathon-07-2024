package github.papi;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.papi.http.HTTP;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PAPI implements ModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger("papi");
	private static final PlayerPIIManager piiManager = new PlayerPIIManager();

	@Override
	public void onInitialize() {
		// Register event handlers for player join and leave
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			ServerWorld world = (ServerWorld) player.getWorld();

			String username = player.getGameProfile().getName();
			try {
				String jsonResponse = HTTP.search(username);

				LOGGER.info("Search response for {}: {}", username, jsonResponse);

				// Check if the response is empty or invalid
				if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.equals("null")) {
					// Clear the player's PII if no user was returned from the HTTP call
					piiManager.clearPlayerPII(player);
					LOGGER.info("No user found for {}: clearing PII", username);
				} else {
					PII playerPII = convertJsonToPlayerPII(jsonResponse);

					// Set the player's PII
					piiManager.setPlayerPII(player, world, playerPII);
				}
			} catch (Exception e) {
				LOGGER.error("Failed to search for user {}: {}", username, e.getMessage());

				// Clear the player's PII in case of an exception
				piiManager.clearPlayerPII(player);
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			piiManager.destroyPlayerPII(player);
		});

		// Register a tick event to update armor stand positions
		ServerTickEvents.END_SERVER_TICK.register(server -> piiManager.updatePlayerPIIPosition());
    }

	private PII convertJsonToPlayerPII(String jsonResponse) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(jsonResponse, PII.class);
	}
}