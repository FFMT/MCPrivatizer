package fr.mcnanotech.privatizer.common;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPrivate extends Block
{
    public static String[] subBlock = new String[] {"stone", "brick", "universal", "furnace", "friendlyStone", "friendlyBrick", "friendlyUniversal", "friendlyFurnace", "passwordStone", "passwordBrick", "passwordUniversal", "passwordFurnace"};
    public String[] furnaceTextureName = new String[] {"top", "side", "front_off", "front_on"};
    public IIcon stoneIcon, brickIcon, universalIcon /* , friendlyStoneIcon, friendlyBrickIcon */, passwordStoneIcon, passwordBrickIcon;
    public IIcon[] furnaceIcon = new IIcon[this.furnaceTextureName.length];
    public IIcon[] passwordFurnaceIcon = new IIcon[this.furnaceTextureName.length];

    protected BlockPrivate(Material material)
    {
        super(material);
    }

    public boolean hasTileEntity(int metadata)
    {
        return metadata >= 0 && metadata <= subBlock.length;
    }

    public TileEntity createTileEntity(World world, int metadata)
    {
        switch(metadata)
        {
            case 0:
                return new TileEntityPrivate();
            case 1:
                return new TileEntityPrivate();
            case 2:
                return new TileEntityPrivateAdaptable();
            case 3:
                return new TileEntityPrivateFurnace();
            case 4:
                return new TileEntityFriend();
            case 5:
                return new TileEntityFriend();
            case 6:
                return new TileEntityFriend();
            case 7:
                return new TileEntityFriend(); // TODO create furnace tile entity friend
            case 8:
                return new TileEntityPassword();
            case 9:
                return new TileEntityPassword();
            case 10:
                return new TileEntityPasswordAdaptable();
            case 11:
                return new TileEntityPasswordFurnace();
            default:
                return null;
        }
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack)
    {
        TileEntity tile = world.getTileEntity(x, y, z);
        int direction = (MathHelper.floor_double((double)(living.rotationYaw * 4.0F / 360.0F) + 2.5D) & 3) + 2;
        if(living instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)living;
            if(stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2 && tile instanceof TileEntityPrivate)
            {
                TileEntityPrivate tilePrivate = (TileEntityPrivate)tile;
                tilePrivate.setOwner(player.getGameProfile());
            }
            else if(stack.getItemDamage() == 3)
            {
                TileEntityPrivateFurnace tilePrivateFurnace = (TileEntityPrivateFurnace)tile;
                tilePrivateFurnace.setOwner(player.getGameProfile());
                tilePrivateFurnace.setDirection((byte)direction);
                if(stack.hasDisplayName())
                {
                    tilePrivateFurnace.setInventoryName(stack.getDisplayName());
                }
            }
            else if(stack.getItemDamage() >= 4 && stack.getItemDamage() <= 7)
            {
                if(tile instanceof TileEntityFriend)
                {
                    TileEntityFriend teFriend = (TileEntityFriend)tile;
                    // TODO friend list
                    // teFriend.setOwners(list);
                }
            }
            else if(stack.getItemDamage() >= 8 && stack.getItemDamage() <= 10 && tile instanceof TileEntityPassword && stack.hasTagCompound())
            {
                NBTTagList list = stack.getTagCompound().getTagList("privatizer", Constants.NBT.TAG_STRING);
                TileEntityPassword tePassword = (TileEntityPassword)tile;
                tePassword.setPassword(list.getStringTagAt(1));
            }
            else if(stack.getItemDamage() == 11 && tile instanceof TileEntityPasswordFurnace)
            {
                TileEntityPasswordFurnace tePassword = (TileEntityPasswordFurnace)tile;
                tePassword.setDirection((byte)direction);
                if(stack.hasDisplayName())
                {
                    tePassword.setInventoryName(stack.getDisplayName());
                }
                if(stack.hasTagCompound())
                {
                    NBTTagList list = stack.getTagCompound().getTagList("privatizer", Constants.NBT.TAG_STRING);
                    tePassword.setPassword(list.getStringTagAt(1));
                }
            }
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if((world.getBlockMetadata(x, y, z) == 2 || world.getBlockMetadata(x, y, z) == 10) && te instanceof IAdaptableTile)
        {
            IAdaptableTile teAdaptable = (IAdaptableTile)te;
            if(player.getCurrentEquippedItem() != null && Block.getBlockFromItem(player.getCurrentEquippedItem().getItem()) != null)
            {
                if(player.getCurrentEquippedItem().getItem() == Item.getItemFromBlock(this) || (player.getCurrentEquippedItem().getItem() == Item.getItemFromBlock(teAdaptable.getBlockForTexture()) && player.getCurrentEquippedItem().getItemDamage() == teAdaptable.getBlockMetadataForTexture()))
                {
                    return false;
                }
                if(Block.getBlockFromItem(player.getCurrentEquippedItem().getItem()).isOpaqueCube())
                {
                    if(teAdaptable instanceof TileEntityPrivateAdaptable)
                    {
                        if(PrivatizerHelper.isOwner(player, ((TileEntityPrivateAdaptable)teAdaptable).getOwner()))
                        {
                            teAdaptable.setStack(player.getCurrentEquippedItem());
                            world.markBlockForUpdate(x, y, z);
                        }
                        else
                        {
                            player.addChatMessage(new ChatComponentTranslation("message.deny.texture.notOwner", ((TileEntityPrivateAdaptable)teAdaptable).getOwner().getName()));
                        }
                    }
                    else if(teAdaptable instanceof TileEntityPasswordAdaptable)
                    {
                        if(PrivatizerHelper.checkPassword(player, ((TileEntityPasswordAdaptable)teAdaptable).getPassword()))
                        {
                            teAdaptable.setStack(player.getCurrentEquippedItem());
                            world.markBlockForUpdate(x, y, z);
                        }
                        else
                        {
                            player.addChatMessage(new ChatComponentTranslation("message.deny.texture.wrongPass"));
                        }
                    }
                }
                else if(!world.isRemote)
                {
                    player.addChatMessage(new ChatComponentTranslation("message.deny.texture"));
                }
            }
            else if(!world.isRemote)
            {
                player.addChatMessage(new ChatComponentTranslation("message.info.texture", teAdaptable.getBlockForTexture() != null ? teAdaptable.getBlockForTexture().getLocalizedName() : "null"));
            }
            return true;
        }

        if(world.getBlockMetadata(x, y, z) == 3 && te instanceof TileEntityPrivateFurnace && !player.isSneaking())
        {
            if(world.isRemote)
            {
                return true;
            }

            TileEntityPrivateFurnace furnace = (TileEntityPrivateFurnace)te;
            if(PrivatizerHelper.isOwner(player, furnace.getOwner()))
            {
                player.openGui(PrivatizerMod.instance, 0, world, x, y, z);
            }
            else
            {
                player.addChatMessage(new ChatComponentTranslation("message.deny.open", furnace.getOwner() != null ? furnace.getOwner().getName() : "unknown"));
            }
            return true;
        }
        if(world.getBlockMetadata(x, y, z) == 11 && te instanceof TileEntityPasswordFurnace && !player.isSneaking())
        {
            if(world.isRemote)
            {
                return true;
            }

            TileEntityPasswordFurnace furnace = (TileEntityPasswordFurnace)te;
            if(PrivatizerHelper.checkPassword(player, furnace.getPassword()))
            {
                player.openGui(PrivatizerMod.instance, 2, world, x, y, z);
            }
            else
            {
                player.addChatMessage(new ChatComponentTranslation("message.password.open"));
            }
            return true;
        }
        return false;
    }

    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityPrivate)
        {
            TileEntityPrivate tePrivate = (TileEntityPrivate)te;
            if(!player.getGameProfile().equals(tePrivate.getOwner()))
            {
                return -1;
            }
        }
        else if(te instanceof TileEntityPassword)
        {
            TileEntityPassword tePass = (TileEntityPassword)te;
            if(!PrivatizerHelper.checkPassword(player, tePass.getPassword()))
            {
                return -1;
            }
        }
        return ForgeHooks.blockStrength(this, player, world, x, y, z);
    }

    public int damageDropped(int metadata)
    {
        return metadata;
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tabs, List list)
    {
        for(int i = 0; i < subBlock.length; i++)
        {
            list.add(new ItemStack(item, 1, i));
        }
    }

    public void registerBlockIcons(IIconRegister iiconRegister)
    {
        this.blockIcon = iiconRegister.registerIcon("privatizer:null");
        this.stoneIcon = iiconRegister.registerIcon("privatizer:private_stone");
        this.brickIcon = iiconRegister.registerIcon("privatizer:private_brick");
        for(int i = 0; i < this.furnaceTextureName.length; i++)
        {
            this.furnaceIcon[i] = iiconRegister.registerIcon(PrivatizerMod.MODID + ":" + "private_furnace_" + this.furnaceTextureName[i]);
        }
        this.universalIcon = iiconRegister.registerIcon("privatizer:private_universal");
        // this.friendlyStoneIcon =
        // iiconRegister.registerIcon("privatizer:friendly_stone");
        // this.friendlyBrickIcon =
        // iiconRegister.registerIcon("privatizer:friendly_brick");
        this.passwordStoneIcon = iiconRegister.registerIcon("privatizer:password_stone");
        this.passwordBrickIcon = iiconRegister.registerIcon("privatizer:password_brick");
        for(int i = 0; i < this.furnaceTextureName.length; i++)
        {
            this.passwordFurnaceIcon[i] = iiconRegister.registerIcon(PrivatizerMod.MODID + ":" + "password_furnace_" + this.furnaceTextureName[i]);
        }
    }

    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
    {
        if(world.getBlockMetadata(x, y, z) == 2 || world.getBlockMetadata(x, y, z) == 10)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof IAdaptableTile)
            {
                IAdaptableTile adaptableTile = (IAdaptableTile)te;
                if(adaptableTile.getBlockForTexture() != null && adaptableTile.getBlockForTexture().isOpaqueCube())
                {
                    return adaptableTile.getBlockForTexture().getIcon(side, adaptableTile.getBlockMetadataForTexture());
                }
            }
        }
        if(world.getBlockMetadata(x, y, z) == 3)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityPrivateFurnace)
            {
                if(side < 2)
                {
                    return this.furnaceIcon[0];
                }
                TileEntityPrivateFurnace furnace = (TileEntityPrivateFurnace)te;
                switch(furnace.getDirection())
                {
                    case 2:
                        return side == 3 ? (furnace.isActive() ? this.furnaceIcon[3] : this.furnaceIcon[2]) : this.furnaceIcon[1];
                    case 3:
                        return side == 4 ? (furnace.isActive() ? this.furnaceIcon[3] : this.furnaceIcon[2]) : this.furnaceIcon[1];
                    case 4:
                        return side == 2 ? (furnace.isActive() ? this.furnaceIcon[3] : this.furnaceIcon[2]) : this.furnaceIcon[1];
                    case 5:
                        return side == 5 ? (furnace.isActive() ? this.furnaceIcon[3] : this.furnaceIcon[2]) : this.furnaceIcon[1];
                }
            }
        }
        if(world.getBlockMetadata(x, y, z) == 11)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityPasswordFurnace)
            {
                if(side < 2)
                {
                    return this.passwordFurnaceIcon[0];
                }
                TileEntityPasswordFurnace furnace = (TileEntityPasswordFurnace)te;
                switch(furnace.getDirection())
                {
                    case 2:
                        return side == 3 ? (furnace.isActive() ? this.passwordFurnaceIcon[3] : this.passwordFurnaceIcon[2]) : this.passwordFurnaceIcon[1];
                    case 3:
                        return side == 4 ? (furnace.isActive() ? this.passwordFurnaceIcon[3] : this.passwordFurnaceIcon[2]) : this.passwordFurnaceIcon[1];
                    case 4:
                        return side == 2 ? (furnace.isActive() ? this.passwordFurnaceIcon[3] : this.passwordFurnaceIcon[2]) : this.passwordFurnaceIcon[1];
                    case 5:
                        return side == 5 ? (furnace.isActive() ? this.passwordFurnaceIcon[3] : this.passwordFurnaceIcon[2]) : this.passwordFurnaceIcon[1];
                }
            }
        }
        return this.getIcon(side, world.getBlockMetadata(x, y, z));
    }

    public IIcon getIcon(int side, int metadata)
    {
        switch(metadata)
        {
            case 0:
                return this.stoneIcon;
            case 1:
                return this.brickIcon;
            case 2:
                return this.universalIcon;
            case 3:
                return side < 2 ? this.furnaceIcon[0] : side == 3 ? this.furnaceIcon[2] : this.furnaceIcon[1];
                // case 4:
                // return this.friendlyStoneIcon;
                // case 5:
                // return this.friendlyBrickIcon;
            case 6:
                return this.universalIcon;
            case 8:
                return this.passwordStoneIcon;
            case 9:
                return this.passwordBrickIcon;
            case 10:
                return this.universalIcon;
            case 11:
                return side < 2 ? this.passwordFurnaceIcon[0] : side == 3 ? this.passwordFurnaceIcon[2] : this.passwordFurnaceIcon[1];

            default:
                return this.blockIcon;
        }
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        if(!world.isRemote)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityPrivate)
            {
                TileEntityPrivate tePrivate = (TileEntityPrivate)te;
                if(!player.getGameProfile().equals(tePrivate.getOwner()))
                {
                    player.addChatMessage(new ChatComponentTranslation("message.deny.break", tePrivate.getOwner() != null ? tePrivate.getOwner().getName() : "unknown"));
                }
            }
            else if(te instanceof TileEntityPassword)
            {
                TileEntityPassword tePass = (TileEntityPassword)te;
                if(!PrivatizerHelper.checkPassword(player, tePass.getPassword()))
                {
                    player.addChatMessage(new ChatComponentTranslation("message.password.break"));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        if(world.getBlockMetadata(target.blockX, target.blockY, target.blockZ) == 2 || world.getBlockMetadata(target.blockX, target.blockY, target.blockZ) == 10)
        {
            TileEntity te = world.getTileEntity(target.blockX, target.blockY, target.blockZ);
            if(te instanceof IAdaptableTile)
            {
                IAdaptableTile tileAdaptable = (IAdaptableTile)te;
                if(tileAdaptable.getBlockForTexture() != null)
                {
                    float f = 0.1F;
                    double d0 = (double)target.blockX + world.rand.nextDouble() * (tileAdaptable.getBlockForTexture().getBlockBoundsMaxX() - tileAdaptable.getBlockForTexture().getBlockBoundsMinX() - (double)(f * 2.0F)) + (double)f + tileAdaptable.getBlockForTexture().getBlockBoundsMinX();
                    double d1 = (double)target.blockY + world.rand.nextDouble() * (tileAdaptable.getBlockForTexture().getBlockBoundsMaxY() - tileAdaptable.getBlockForTexture().getBlockBoundsMinY() - (double)(f * 2.0F)) + (double)f + tileAdaptable.getBlockForTexture().getBlockBoundsMinY();
                    double d2 = (double)target.blockZ + world.rand.nextDouble() * (tileAdaptable.getBlockForTexture().getBlockBoundsMaxZ() - tileAdaptable.getBlockForTexture().getBlockBoundsMinZ() - (double)(f * 2.0F)) + (double)f + tileAdaptable.getBlockForTexture().getBlockBoundsMinZ();

                    if(target.sideHit == 0)
                    {
                        d1 = (double)target.blockY + tileAdaptable.getBlockForTexture().getBlockBoundsMinY() - (double)f;
                    }

                    if(target.sideHit == 1)
                    {
                        d1 = (double)target.blockY + tileAdaptable.getBlockForTexture().getBlockBoundsMaxY() + (double)f;
                    }

                    if(target.sideHit == 2)
                    {
                        d2 = (double)target.blockZ + tileAdaptable.getBlockForTexture().getBlockBoundsMinZ() - (double)f;
                    }

                    if(target.sideHit == 3)
                    {
                        d2 = (double)target.blockZ + tileAdaptable.getBlockForTexture().getBlockBoundsMaxZ() + (double)f;
                    }

                    if(target.sideHit == 4)
                    {
                        d0 = (double)target.blockX + tileAdaptable.getBlockForTexture().getBlockBoundsMinX() - (double)f;
                    }

                    if(target.sideHit == 5)
                    {
                        d0 = (double)target.blockX + tileAdaptable.getBlockForTexture().getBlockBoundsMaxX() + (double)f;
                    }

                    effectRenderer.addEffect((new EntityDiggingFX(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, tileAdaptable.getBlockForTexture(), tileAdaptable.getBlockMetadataForTexture())).applyColourMultiplier(target.blockX, target.blockY, target.blockZ).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
                    return true;
                }
            }
        }
        return super.addHitEffects(world, target, effectRenderer);
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        if(world.getBlockMetadata(x, y, z) == 2 || world.getBlockMetadata(x, y, z) == 10)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof IAdaptableTile)
            {
                IAdaptableTile tileAdaptable = (IAdaptableTile)te;
                if(tileAdaptable.getBlockForTexture() != null)
                {
                    byte b0 = 4;

                    for(int i1 = 0; i1 < b0; ++i1)
                    {
                        for(int j1 = 0; j1 < b0; ++j1)
                        {
                            for(int k1 = 0; k1 < b0; ++k1)
                            {
                                double d0 = (double)x + ((double)i1 + 0.5D) / (double)b0;
                                double d1 = (double)y + ((double)j1 + 0.5D) / (double)b0;
                                double d2 = (double)z + ((double)k1 + 0.5D) / (double)b0;
                                effectRenderer.addEffect((new EntityDiggingFX(world, d0, d1, d2, d0 - (double)x - 0.5D, d1 - (double)y - 0.5D, d2 - (double)z - 0.5D, tileAdaptable.getBlockForTexture(), tileAdaptable.getBlockMetadataForTexture())).applyColourMultiplier(x, y, z));
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return super.addDestroyEffects(world, x, y, z, meta, effectRenderer);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
        TileEntity tile = world.getTileEntity(x, y, z);
        if(world.getBlockMetadata(x, y, z) == 3 && tile instanceof TileEntityPrivateFurnace && ((TileEntityPrivateFurnace)tile).isActive())
        {
            this.spawnParticle(world, x, y, z, rand, ((TileEntityPrivateFurnace)tile).getDirection());
        }
        if(world.getBlockMetadata(x, y, z) == 11 && tile instanceof TileEntityPasswordFurnace && ((TileEntityPasswordFurnace)tile).isActive())
        {
            this.spawnParticle(world, x, y, z, rand, ((TileEntityPasswordFurnace)tile).getDirection());
        }
    }

    public void spawnParticle(World world, double x, double y, double z, Random rand, byte side)
    {
        float xCenter = (float)x + 0.5F;
        float ySpawn = (float)y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
        float zCenter = (float)z + 0.5F;
        float randF = rand.nextFloat() * 0.6F - 0.3F;

        if(side == 3)
        {
            world.spawnParticle("smoke", xCenter - 0.52F, ySpawn, zCenter + randF, 0.0D, 0.0D, 0.0D);
            world.spawnParticle("flame", xCenter - 0.52F, ySpawn, zCenter + randF, 0.0D, 0.0D, 0.0D);
        }
        else if(side == 5)
        {
            world.spawnParticle("smoke", xCenter + 0.52F, ySpawn, zCenter + randF, 0.0D, 0.0D, 0.0D);
            world.spawnParticle("flame", xCenter + 0.52F, ySpawn, zCenter + randF, 0.0D, 0.0D, 0.0D);
        }
        else if(side == 4)
        {
            world.spawnParticle("smoke", xCenter + randF, ySpawn, zCenter - 0.52F, 0.0D, 0.0D, 0.0D);
            world.spawnParticle("flame", xCenter + randF, ySpawn, zCenter - 0.52F, 0.0D, 0.0D, 0.0D);
        }
        else if(side == 2)
        {
            world.spawnParticle("smoke", xCenter + randF, ySpawn, zCenter + 0.52F, 0.0D, 0.0D, 0.0D);
            world.spawnParticle("flame", xCenter + randF, ySpawn, zCenter + 0.52F, 0.0D, 0.0D, 0.0D);
        }
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
    {
        if(metadata == 3 || metadata == 11)
        {
            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof IInventory)
            {
                IInventory inv = (IInventory)tile;
                for(int i1 = 0; i1 < inv.getSizeInventory(); ++i1)
                {
                    ItemStack itemstack = inv.getStackInSlot(i1);

                    if(itemstack != null)
                    {
                        float f = world.rand.nextFloat() * 0.8F + 0.1F;
                        float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                        float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

                        while(itemstack.stackSize > 0)
                        {
                            int j1 = world.rand.nextInt(21) + 10;

                            if(j1 > itemstack.stackSize)
                            {
                                j1 = itemstack.stackSize;
                            }

                            itemstack.stackSize -= j1;
                            EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                            if(itemstack.hasTagCompound())
                            {
                                entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                            }

                            float f3 = 0.05F;
                            entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
                            entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
                            entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);
                            world.spawnEntityInWorld(entityitem);
                        }
                    }
                }
                world.func_147453_f(x, y, z, block);
            }
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    @SideOnly(Side.CLIENT)
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
        if(world.getBlockMetadata(x, y, z) == 3 || world.getBlockMetadata(x, y, z) == 11)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof IFurnaceTile && ((IFurnaceTile)te).isActive())
            {
                return 13;
            }
        }
        return super.getLightValue(world, x, y, z);
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis)
    {
        if((axis == ForgeDirection.UP || axis == ForgeDirection.DOWN) && !world.isRemote && world.getBlockMetadata(x, y, z) == 3)
        {
            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof IDirectionalTile)
            {
                IDirectionalTile tileDirectional = (IDirectionalTile)tile;
                byte direction = tileDirectional.getDirection();
                direction++;
                if(direction > 5)
                {
                    direction = 2;
                }
                tileDirectional.setDirection(direction);
                return true;
            }
        }
        return false;
    }

    public ForgeDirection[] getValidRotations(World world, int x, int y, int z)
    {
        if(world.getBlockMetadata(x, y, z) == 3 || world.getBlockMetadata(x, y, z) == 11)
        {
            return new ForgeDirection[] {ForgeDirection.UP, ForgeDirection.DOWN};
        }
        return ForgeDirection.VALID_DIRECTIONS;
    }

    public boolean onBlockEventReceived(World world, int x, int y, int z, int eventId, int value)
    {
        super.onBlockEventReceived(world, x, y, z, eventId, value);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        return tileentity != null ? tileentity.receiveClientEvent(eventId, value) : false;
    }
}