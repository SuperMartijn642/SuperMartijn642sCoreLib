package com.supermartijn642.core.block;

import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class BaseBlock extends Block {

    private static final ResourceLocation MINEABLE_WITH_AXE = new ResourceLocation("mineable/axe");
    private static final ResourceLocation MINEABLE_WITH_HOE = new ResourceLocation("mineable/hoe");
    private static final ResourceLocation MINEABLE_WITH_PICKAXE = new ResourceLocation("mineable/pickaxe");
    private static final ResourceLocation MINEABLE_WITH_SHOVEL = new ResourceLocation("mineable/shovel");
    private static final ResourceLocation NEEDS_DIAMOND_TOOL = new ResourceLocation("needs_diamond_tool");
    private static final ResourceLocation NEEDS_IRON_TOOL = new ResourceLocation("needs_iron_tool");
    private static final ResourceLocation NEEDS_STONE_TOOL = new ResourceLocation("needs_stone_tool");

    private final boolean saveTileData;
    private final BlockProperties properties;

    public BaseBlock(boolean saveTileData, BlockProperties properties){
        super(properties.material, properties.mapColor);
        this.saveTileData = saveTileData;
        this.properties = properties;

        this.setSoundType(properties.soundType);
        this.setResistance(properties.explosionResistance);
        this.setHardness(properties.destroyTime);
        this.setTickRandomly(properties.ticksRandomly);
        this.setDefaultSlipperiness(properties.friction);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        NBTTagCompound tag = stack.getTagCompound();
        tag = tag == null ? null : tag.hasKey("tileData") ? tag.getCompoundTag("tileData") : null;
        if(tag == null || tag.hasNoTags())
            return;

        TileEntity entity = worldIn.getTileEntity(pos);
        if(entity instanceof BaseBlockEntity)
            ((BaseBlockEntity)entity).readData(tag);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune){
        return this.properties.noLootTable ? Items.AIR : this.properties.lootTableBlock != null ? this.properties.lootTableBlock.get().getItemDropped(state, random, fortune) : super.getItemDropped(state, random, fortune);
    }

    @Override
    public int damageDropped(IBlockState state){
        return this.properties.noLootTable ? 0 : this.properties.lootTableBlock != null ? this.properties.lootTableBlock.get().damageDropped(state) : super.damageDropped(state);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random){
        return this.properties.noLootTable ? 0 : this.properties.lootTableBlock != null ? this.properties.lootTableBlock.get().quantityDropped(state, fortune, random) : super.quantityDropped(state, fortune, random);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        drops.addAll(this.getActualDrops(world, pos, state, fortune));
    }

    public List<ItemStack> getActualDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        NonNullList<ItemStack> drops = NonNullList.create();

        super.getDrops(drops, world, pos, state, fortune);

        if(!this.saveTileData)
            return drops;

        TileEntity entity = world.getTileEntity(pos);
        if(!(entity instanceof BaseBlockEntity))
            return drops;

        if(((BaseBlockEntity)entity).destroyedByCreativePlayer)
            return NonNullList.create();

        NBTTagCompound tileTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(tileTag == null || tileTag.hasNoTags())
            return drops;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", tileTag);

        for(ItemStack stack : drops){
            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this){
                stack.setTagCompound(tag);
            }
        }

        return drops;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        TileEntity entity = world.getTileEntity(pos);
        if(!(entity instanceof BaseBlockEntity))
            return stack;

        NBTTagCompound entityTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(entityTag == null || entityTag.hasNoTags())
            return stack;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", entityTag);

        if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this)
            stack.setTagCompound(tag);

        return stack;
    }

    @Override
    public boolean onBlockActivated(World level, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ){
        return this.interact(state, level, pos, player, hand, hitSide, new Vec3d(hitX, hitY, hitZ)).consumesAction();
    }

    /**
     * Called when a player interacts with this block.
     * @return whether the player's interaction should be consumed or passed on
     */
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        return InteractionFeedback.PASS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World level, List<String> information, ITooltipFlag flag){
        this.appendItemInformation(stack, level, component -> information.add(component.getFormattedText()), flag.isAdvanced());
        super.addInformation(stack, level, information, flag);
    }

    /**
     * Adds information to be displayed when hovering over the item corresponding to this block in the inventory.
     * @param stack    the stack being hovered over
     * @param level    the world the player is in, may be {@code null}
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
    }

    @Override
    public boolean isToolEffective(String tool, IBlockState state){
        return ("axe".equals(tool) && this.is(MINEABLE_WITH_AXE))
            || ("hoe".equals(tool) && this.is(MINEABLE_WITH_HOE))
            || ("pickaxe".equals(tool) && this.is(MINEABLE_WITH_PICKAXE))
            || ("shovel".equals(tool) && this.is(MINEABLE_WITH_SHOVEL));
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state){
        return this.is(MINEABLE_WITH_AXE) ? "axe"
            : this.is(MINEABLE_WITH_HOE) ? "hoe"
            : this.is(MINEABLE_WITH_PICKAXE) ? "pickaxe"
            : this.is(MINEABLE_WITH_SHOVEL) ? "shovel"
            : null;
    }

    @Override
    public int getHarvestLevel(IBlockState state){
        return this.is(NEEDS_DIAMOND_TOOL) ? 3
            : this.is(NEEDS_IRON_TOOL) ? 2
            : this.is(NEEDS_STONE_TOOL) ? 1
            : -1;
    }

    private boolean is(ResourceLocation tag){
        return TagLoader.getTag(Registries.BLOCKS, tag).contains(Registries.BLOCKS.getIdentifier(this));
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos){
        return this.properties.isAir;
    }

    @Override
    public int getLightValue(IBlockState state){
        return this.properties.lightLevel.applyAsInt(state);
    }

    @Override
    public boolean causesSuffocation(IBlockState state){
        return this.properties.isSuffocating.test(state);
    }

    public final boolean requiresCorrectToolForDrops(){
        return this.properties.requiresCorrectTool;
    }

    public float getSpeedFactor(){
        return this.properties.speedFactor;
    }

    public float getJumpFactor(){
        return this.properties.jumpFactor;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return this.properties.canOcclude;
    }

    @Override
    public boolean isCollidable(){
        return this.properties.hasCollision;
    }

    @Override
    public String getLocalizedName(){
        return I18n.format(this.getUnlocalizedName()).trim();
    }

    @Override
    public String getUnlocalizedName(){
        return this.getRegistryName().getResourceDomain() + ".block." + this.getRegistryName().getResourcePath();
    }

    protected enum InteractionFeedback {
        PASS, CONSUME, SUCCESS;

        private boolean consumesAction(){
            return this == SUCCESS || this == CONSUME;
        }
    }
}
