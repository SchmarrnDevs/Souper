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

import dev.schmarrn.souper.blocks.PotEntity;
import dev.schmarrn.souper.blocks.SouperBlocks;
import dev.schmarrn.souper.items.SoupItem;
import dev.schmarrn.souper.items.SouperItems;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class SouperClient implements ClientModInitializer {
	public static final int COLOR_NORMAL = 0x1e97e8;

	@Override
	public void onInitializeClient(ModContainer mod) {
		ColorProviderRegistry.BLOCK.register((blockState, blockRenderView, blockPos, i) -> {
			// Totally not embarrasing that I forgot to remove this - I love you sodium devs, don't worry, this was just an
			// WHAT THE ACTUAL F - moment
			// Souper.LOGGER.info("WHY THE FUCK SODIUM IS THIS NOT A POTENTITY - FUCK YOU: {}", blockRenderView.getBlockEntity(blockPos));
			if (blockRenderView != null && blockRenderView.getBlockEntity(blockPos) instanceof PotEntity entity) {
				if (i == 1) return entity.getTargetColor();

				switch (entity.getState()) {
					case PotEntity.COOKING -> {
						int colorDone = entity.getTargetColor();
						// Not so horrible but still pretty terrible interpolated color
						int dr = ((colorDone & 0xFF0000) >> 16) - ((COLOR_NORMAL & 0xFF0000) >> 16);
						int dg = ((colorDone & 0x00FF00) >> 8) - ((COLOR_NORMAL & 0x00FF00) >> 8);
						int db = (colorDone & 0xFF) - (COLOR_NORMAL & 0xFF);

						int col = (((int)((float) dr / 400f * (float)entity.getTick() + ((COLOR_NORMAL & 0xFF0000) >> 16)) & 0xFF) << 16)
								| (((int)((float) dg / 400f * (float)entity.getTick() + ((COLOR_NORMAL & 0xFF00) >> 8)) & 0xFF) << 8)
								| (int)((float) db / 400f * (float)entity.getTick() + (COLOR_NORMAL & 0xFF)) & 0xFF;

						return col;
					}
					case PotEntity.DONE -> {
						return entity.getTargetColor();
					}
					case PotEntity.EMPTY, PotEntity.READY -> {
						return COLOR_NORMAL;
					}
				}
			}

			return 0xFFFFFF;
		}, SouperBlocks.POT);

		ColorProviderRegistry.ITEM.register((itemStack, i) -> {
			if (i == 1 && itemStack.getItem() instanceof SoupItem soup) {
				return soup.getColor();
			}
			return 0xFFFFFF;
		}, SouperItems.getSoup().toArray(new Item[0]));
	}
}
