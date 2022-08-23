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

import dev.schmarrn.souper.items.SouperItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PotBlock extends BlockWithEntity implements BlockEntityProvider {
	private final boolean interactable;

	public static final IntProperty WATER = IntProperty.of("water", 0, 3);

	public static final BooleanProperty HAS_STUFF = BooleanProperty.of("stuff");

	public PotBlock(Settings settings, boolean interactable) {
		super(settings.nonOpaque().dynamicBounds());
		this.interactable = interactable;
		setDefaultState(getStateManager().getDefaultState().with(WATER, 0).with(HAS_STUFF, false));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, SouperBlocks.POT_ENTITY, PotEntity::tick);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATER).add(HAS_STUFF);
	}

	private static boolean isValidIngredient(Item item) {
		return item.equals(Items.CARROT)
				|| item.equals(Items.POTATO)
				|| item.equals(Items.BROWN_MUSHROOM)
				|| item.equals(Items.RED_MUSHROOM)
				|| item.equals(Items.DRAGON_EGG)
				|| item.equals(Items.DRAGON_HEAD)
				|| item.equals(Items.BEETROOT)
				|| item.equals(Items.APPLE)
				|| item.equals(Items.CHICKEN)
				|| item.equals(Items.DRAGON_BREATH)
				|| item.equals(Items.BEEF)
				|| item.equals(Items.PORKCHOP);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!interactable) return ActionResult.PASS;

		Item inMainHand = player.getMainHandStack().getItem();

		if (world.getBlockEntity(pos) instanceof PotEntity entity) {
			switch (entity.getState()) {
				case PotEntity.EMPTY -> {
					if (inMainHand.equals(Items.WATER_BUCKET)) {
						if (world.isClient) return ActionResult.SUCCESS;

						int slot = player.getInventory().selectedSlot;
						player.getInventory().setStack(slot, new ItemStack(Items.BUCKET));
						world.setBlockState(pos, state.with(WATER, 3));

						entity.setState(PotEntity.READY);

						return ActionResult.SUCCESS;
					}
				}
				case PotEntity.READY -> {
					if (isValidIngredient(inMainHand)) {
						if (!entity.hasIngredient()) {
							if (world.isClient) return ActionResult.SUCCESS;

							if (Items.CARROT.equals(inMainHand)) {
								entity.setOutput(SouperItems.CARROT_SOUP);
							} else if (inMainHand.equals(Items.POTATO)) {
								entity.setOutput(SouperItems.POTATO_SOUP);
							} else if (inMainHand.equals(Items.BROWN_MUSHROOM) || inMainHand.equals(Items.RED_MUSHROOM)) {
								entity.setOutput(SouperItems.SHROOM_SOUP);
							} else if (inMainHand.equals(Items.DRAGON_EGG) || inMainHand.equals(Items.DRAGON_HEAD)) {
								entity.setOutput(SouperItems.DRAGON_SOUP);
							} else if (inMainHand.equals(Items.BEETROOT)) {
								entity.setOutput(SouperItems.BEETROOT_SOUP);
							} else if (inMainHand.equals(Items.APPLE)) {
								entity.setOutput(SouperItems.APPLE_SAUCE);
							} else if (inMainHand.equals(Items.CHICKEN)) {
								entity.setOutput(SouperItems.CHICKEN_SOUP);
							} else if (inMainHand.equals(Items.DRAGON_BREATH)) {
								entity.setOutput(SouperItems.SPECIAL_AIR_SOUP);
							} else if (inMainHand.equals(Items.PORKCHOP)) {
								entity.setOutput(SouperItems.SCHWEINSGULASCH);
							} else if (inMainHand.equals(Items.BEEF)) {
								entity.setOutput(SouperItems.RINDSGULASCH);
							}
							player.getMainHandStack().decrement(1);

							entity.setState(PotEntity.COOKING);
							world.setBlockState(pos, state.with(HAS_STUFF, true));
							return ActionResult.SUCCESS;
						} else {
							return ActionResult.PASS;
						}
					}
				}
				case PotEntity.DONE -> {
					if (state.get(WATER) != 0 && inMainHand.equals(Items.BOWL)) {
						if (world.isClient) return ActionResult.SUCCESS;
						player.getMainHandStack().decrement(1);

						if (!player.getInventory().insertStack(entity.getOneServing())) {
							player.dropItem(entity.getOneServing(), true);
						}

						int newWaterLevel = state.get(WATER)-1;

						if (newWaterLevel == 0) {
							world.setBlockState(pos, state.with(WATER, newWaterLevel).with(HAS_STUFF, false));
							entity.clear();
						} else {
							world.setBlockState(pos, state.with(WATER, newWaterLevel));
						}
						return ActionResult.SUCCESS;
					}
				}
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0, 0.25, 0.75, 0.0625, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.0625, 0.25, 0.8125, 0.3125, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.0625, 0.1875, 0.75, 0.3125, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.0625, 0.75, 0.75, 0.3125, 0.8125));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.0625, 0.25, 0.25, 0.3125, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 0.1875, 0.375, 0.875, 0.25, 0.625));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.1875, 0.375, 0.1875, 0.25, 0.625));

		return shape;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PotEntity(pos, state);
	}
}
