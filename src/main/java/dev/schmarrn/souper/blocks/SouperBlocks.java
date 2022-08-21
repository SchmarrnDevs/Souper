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

package dev.schmarrn.souper.blocks;

import dev.schmarrn.souper.Souper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class SouperBlocks {
	public static final Block RAW_POT = new PotBlock(QuiltBlockSettings.copyOf(Blocks.CLAY), false);
	public static final Block POT = new PotBlock(QuiltBlockSettings.copyOf(Blocks.TERRACOTTA), true);

	public static final BlockEntityType<PotEntity> POT_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			new Identifier(Souper.MOD_ID, "pot_block_entity"),
			QuiltBlockEntityTypeBuilder.create(PotEntity::new, POT).build()
	);

	public static final ItemGroup itemGroup = QuiltItemGroup.createWithIcon(new Identifier(Souper.MOD_ID, "souper"), () -> POT.asItem().getDefaultStack());

	private static void register(String name, Block block) {
		Registry.register(Registry.BLOCK, new Identifier(Souper.MOD_ID, name), block);
		Registry.register(Registry.ITEM, new Identifier(Souper.MOD_ID, name), new BlockItem(block, new QuiltItemSettings().group(itemGroup)));
	}

	public static void init() {
		register("raw_pot", RAW_POT);
		register("pot", POT);
	}
}
