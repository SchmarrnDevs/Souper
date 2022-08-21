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

package dev.schmarrn.souper.items;

import dev.schmarrn.souper.Souper;
import dev.schmarrn.souper.blocks.SouperBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.ArrayList;
import java.util.List;

public class SouperItems {
	private static final List<Item> items = new ArrayList<>();

	public static void init() {
		// init for static thingies
	}

	private static Item register(String name, int hunger, int color, boolean infinite) {
		SoupItem soupItem = new SoupItem(new QuiltItemSettings().group(SouperBlocks.itemGroup), hunger, color, infinite);
		Registry.register(Registry.ITEM, new Identifier(Souper.MOD_ID, name), soupItem);
		items.add(soupItem);
		return soupItem;
	}

	private static Item register(String name, int hunger, int color) {
		return register(name, hunger, color, false);
	}

	public static final Item CARROT_SOUP = register("carrot_soup", Items.CARROT.getFoodComponent().getHunger() * 2, 0xf49e1d);
	public static final Item POTATO_SOUP = register("potato_soup", Items.BAKED_POTATO.getFoodComponent().getHunger() * 2, 0xe8d176);
	public static final Item SHROOM_SOUP = register("shroom_soup", Items.MUSHROOM_STEW.getFoodComponent().getHunger(), 0x8e6536);
	public static final Item DRAGON_SOUP = register("dragon_soup", Items.COOKED_BEEF.getFoodComponent().getHunger() * 5, 0x440b60, true);
	public static final Item BEETROOT_SOUP = register("beetroot_soup", Items.BEETROOT_SOUP.getFoodComponent().getHunger(), 0xb72610);
	public static final Item APPLE_SAUCE = register("apple_sauce", Items.APPLE.getFoodComponent().getHunger() * 2, 0xefd739);
	public static final Item CHICKEN_SOUP = register("chicken_soup", Items.COOKED_CHICKEN.getFoodComponent().getHunger() * 2, 0xefe6bd);
	public static final Item SPECIAL_AIR_SOUP = register("special_air_soup", Items.COOKED_BEEF.getFoodComponent().getHunger(), 0xd042f7);
	public static final Item RINDSGULASCH = register("rindsgulasch", Items.COOKED_BEEF.getFoodComponent().getHunger()*2, 0x77261a);
	public static final Item SCHWEINSGULASCH = register("schweinsgulasch", Items.COOKED_PORKCHOP.getFoodComponent().getHunger()*2, 0x772d4b);

	public static List<Item> getSoup() {
		return items;
	}
}
