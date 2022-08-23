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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class SoupItem extends Item {
	private final int color;
	private final boolean isInfinite;

	public SoupItem(Settings settings, int hunger, int color, boolean isInfinite) {
		super(settings.food(
				new FoodComponent.Builder()
						.meat()
						.hunger(hunger)
						.build()
				)
				.maxDamage(1)
		);
		this.color = color;
		this.isInfinite = isInfinite;
	}

	public int getColor() {
		return color;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		if (isInfinite) {
			return Rarity.EPIC;
		}
		return super.getRarity(stack);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return isInfinite;
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		ItemStack itemStack = super.finishUsing(stack, world, user);

		if (stack.getItem() instanceof SoupItem item) {
			if (user instanceof PlayerEntity && item.isInfinite) {
				return new ItemStack(item);
			}
		}

		return user instanceof PlayerEntity && (((PlayerEntity)user).getAbilities().creativeMode) ? itemStack : new ItemStack(Items.BOWL);
	}
}
