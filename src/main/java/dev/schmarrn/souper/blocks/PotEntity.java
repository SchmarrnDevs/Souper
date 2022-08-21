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

import dev.schmarrn.souper.items.SoupItem;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PotEntity extends BlockEntity {
	public static final int COLOR_NORMAL = 0x1e97e8;

	private Item ingredient = Items.AIR;

	private int tick = 0;

	public static final int EMPTY = 0;
	public static final int READY = 1;
	public static final int COOKING = 2;
	public static final int DONE = 3;

	private int state = EMPTY;

	public PotEntity(BlockPos blockPos, BlockState blockState) {
		super(SouperBlocks.POT_ENTITY, blockPos, blockState);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		ingredient = Registry.ITEM.get(new Identifier(nbt.getString("ingredient")));
		state = nbt.getInt("state");
		tick = nbt.getInt("tick");


		if (world != null && this.world.isClient) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.execute(() -> {
				world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
			});
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		nbt.putString("ingredient", Registry.ITEM.getId(ingredient).toString());
		nbt.putInt("state", state);
		nbt.putInt("tick", tick);
		super.writeNbt(nbt);
	}

	public boolean hasIngredient() {
		return ingredient != Items.AIR;
	}

	public void clear() {
		ingredient = Items.AIR;
		state = EMPTY;
	}

	public void setOutput(Item soupItem) {
		ingredient = soupItem;
	}

	public ItemStack getOneServing() {
		return new ItemStack(ingredient);
	}

	public void setState(int state) {
		if (state >= EMPTY && state <= DONE) {
			this.state = state;
			this.markDirty();
		}
	}

	public int getTargetColor() {
		if (ingredient instanceof SoupItem soup) {
			return soup.getColor();
		} else {
			return 0;
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isClient()) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound nbt = super.toInitialChunkDataNbt();
		writeNbt(nbt);
		return nbt;
	}

	public int getTick() {
		return tick;
	}

	public int getState() {
		return state;
	}

	public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
		PotEntity entity = (PotEntity) blockEntity;
		if (entity == null) return;

		if (entity.getState() == COOKING) {
			Block below = world.getBlockState(blockPos.down()).getBlock();
			if (below.equals(Blocks.FURNACE) || below.equals(Blocks.SMOKER) || below.equals(Blocks.BLAST_FURNACE)) {
				boolean furnaceIsLit = world.getBlockState(blockPos.down()).get(AbstractFurnaceBlock.LIT);

				if (furnaceIsLit) {
					if (world.isClient) {
						double rand = world.getRandom().nextDouble();
						if (rand > 0.5) return;

						int colorDone = entity.getTargetColor();
						// Not so horrible but still pretty terrible interpolated color
						int dr = ((colorDone & 0xFF0000) >> 16) - ((COLOR_NORMAL & 0xFF0000) >> 16);
						int dg = ((colorDone & 0x00FF00) >> 8) - ((COLOR_NORMAL & 0x00FF00) >> 8);
						int db = (colorDone & 0xFF) - (COLOR_NORMAL & 0xFF);

						int r = ((int)((float) dr / 400f * (float)entity.tick + ((COLOR_NORMAL & 0xFF0000) >> 16)) & 0xFF);
						int g = ((int)((float) dg / 400f * (float)entity.tick + ((COLOR_NORMAL & 0xFF00) >> 8)) & 0xFF);
						int b = (int)((float) db / 400f * (float)entity.tick + (COLOR_NORMAL & 0xFF)) & 0xFF;

						world.addParticle(ParticleTypes.ENTITY_EFFECT, blockPos.getX() + 0.25 + rand/2, blockPos.getY()+0.15, blockPos.getZ() + 0.25 + rand/2, r / 255f, g / 255f, b / 255f);
					} else {
						entity.tick++;

						// You need to smelt 2 items for the soup to be cooked
						if (entity.tick >= 400) {
							entity.tick = 0;
							entity.setState(DONE);
						}

						entity.markDirty();
					}
				} else {
					// If furnace is not lit and soup is not done, decrement ticks again
					if (!world.isClient && entity.tick > 0) {
						entity.tick--;
						entity.markDirty();
					}
				}
			}
		}
	}
}
