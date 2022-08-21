/*
 * Copyright 2022 Andreas Kohler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.schmarrn.souper;

import dev.schmarrn.souper.blocks.SouperBlocks;
import dev.schmarrn.souper.items.SouperItems;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Souper implements ModInitializer {
	public static final String MOD_ID = "souper";
	public static final String MOD_NAME = "Souper";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Let's get cooking!");

		SouperBlocks.init();
		SouperItems.init();
	}
}
