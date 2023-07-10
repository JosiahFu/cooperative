package net.qxeii.cooperative;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cooperative implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("cooperative");

	@Override
	public void onInitialize() {

		LOGGER.info("It's cooperating time.");
	}
}