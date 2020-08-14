package xyz.phanta.fluiddrawers.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.jaquadro.minecraft.chameleon.resources.IconUtil;
import io.github.phantamanta44.libnine.client.model.L9Models;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.render.model.BakedQuadList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;
import net.minecraftforge.common.property.IExtendedBlockState;
import xyz.phanta.fluiddrawers.client.model.base.DelegatingBakedModel;
import xyz.phanta.fluiddrawers.client.util.FramedModelData;
import xyz.phanta.fluiddrawers.item.base.FramedItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FramedTextureModel implements IModel {


    private final ResourceLocation backingModelLoc;
    private final IModel backingModel;
    private final ResourceLocation defTexFront, defTexSide, defTexTrim;

    public FramedTextureModel(ResourceLocation backingModelLoc, IModel backingModel,
                              ResourceLocation defTexFront, ResourceLocation defTexSide, ResourceLocation defTexTrim) {
        this.backingModelLoc = backingModelLoc;
        this.backingModel = backingModel;
        this.defTexFront = defTexFront;
        this.defTexSide = defTexSide;
        this.defTexTrim = defTexTrim;
    }

    private IModel map(UnaryOperator<IModel> mapper) {
        return new FramedTextureModel(backingModelLoc, mapper.apply(backingModel), defTexFront, defTexSide, defTexTrim);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(backingModelLoc);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Stream.concat(backingModel.getTextures().stream(), Stream.of(defTexFront, defTexSide, defTexTrim))
                .collect(Collectors.toSet());
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
                            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedFramedTextureModel(backingModel.bake(state, format, new FramingTextureMapper(bakedTextureGetter)),
                bakedTextureGetter.apply(defTexFront), bakedTextureGetter.apply(defTexSide), bakedTextureGetter.apply(defTexTrim));
    }

    @Override
    public IModelState getDefaultState() {
        return backingModel.getDefaultState();
    }

    @Override
    public Optional<? extends IClip> getClip(String name) {
        return backingModel.getClip(name);
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        return map(m -> m.process(customData));
    }

    @Override
    public IModel smoothLighting(boolean value) {
        return map(m -> m.smoothLighting(value));
    }

    @Override
    public IModel gui3d(boolean value) {
        return map(m -> m.gui3d(value));
    }

    @Override
    public IModel uvlock(boolean value) {
        return map(m -> m.uvlock(value));
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        return new FramedTextureModel(backingModelLoc, backingModel.retexture(textures),
                mapTex(textures, defTexFront), mapTex(textures, defTexSide), mapTex(textures, defTexTrim));
    }

    private static ResourceLocation mapTex(ImmutableMap<String, String> mapping, ResourceLocation tex) {
        String newTex = mapping.get(tex.toString());
        if (newTex == null) {
            return tex;
        } else if (newTex.isEmpty()) {
            return TextureMap.LOCATION_MISSING_TEXTURE;
        } else {
            return new ResourceLocation(newTex);
        }
    }

    @Override
    public Optional<ModelBlock> asVanillaModel() {
        return backingModel.asVanillaModel();
    }

    public static class Loader implements ICustomModelLoader {

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            try {
                JsonObject dto = ResourceUtils.getAsJson(L9Models.getRealModelLocation(modelLocation)).getAsJsonObject();
                return dto.has("9s") && dto.get("9s").getAsString().equals("fluiddrawers:framed");
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            JsonObject dto = ResourceUtils.getAsJson(L9Models.getRealModelLocation(modelLocation)).getAsJsonObject();
            ResourceLocation backingModelLoc = new ResourceLocation(dto.get("backing").getAsString());
            JsonObject defTexDto = dto.getAsJsonObject("default_textures");
            return new FramedTextureModel(backingModelLoc, ModelLoaderRegistry.getModel(backingModelLoc),
                    new ResourceLocation(defTexDto.get("front").getAsString()),
                    new ResourceLocation(defTexDto.get("side").getAsString()),
                    new ResourceLocation(defTexDto.get("trim").getAsString()));
        }

    }

    private static class FramingTextureMapper implements Function<ResourceLocation, TextureAtlasSprite> {

        public static final TextureAtlasSprite MARKER_SPRITE_FRONT = new SentinelTextureAtlasSprite();
        public static final TextureAtlasSprite MARKER_SPRITE_SIDE = new SentinelTextureAtlasSprite();
        public static final TextureAtlasSprite MARKER_SPRITE_TRIM = new SentinelTextureAtlasSprite();

        private final Function<ResourceLocation, TextureAtlasSprite> downstream;

        FramingTextureMapper(Function<ResourceLocation, TextureAtlasSprite> downstream) {
            this.downstream = downstream;
        }

        @Override
        public TextureAtlasSprite apply(ResourceLocation texPath) {
            if (texPath.getNamespace().equals("fluiddrawers")) {
                switch (texPath.getPath()) {
                    case "generated/framing/front":
                        return MARKER_SPRITE_FRONT;
                    case "generated/framing/side":
                        return MARKER_SPRITE_SIDE;
                    case "generated/framing/trim":
                        return MARKER_SPRITE_TRIM;
                }
            }
            return downstream.apply(texPath);
        }

        private static class SentinelTextureAtlasSprite extends TextureAtlasSprite {

            SentinelTextureAtlasSprite() {
                super("");
                width = height = 16; // some made-up numbers
                initSprite(16, 16, 0, 0, false); // must be nonzero to make sure uv calculation is nonsingular
            }

        }

    }

    private static class BakedFramedTextureModel extends DelegatingBakedModel {

        private final IBakedModel backing;
        private final TextureAtlasSprite defSpriteFront, defSpriteSide, defSpriteTrim;
        private final Cache<FramedModelData, FramedModelInstance> instanceCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // cache params borrowed from Tinkers' Construct model system, which is under MIT
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        private final FramedModelItemMapper itemMapper = new FramedModelItemMapper();

        BakedFramedTextureModel(IBakedModel backing, TextureAtlasSprite defSpriteFront,
                                TextureAtlasSprite defSpriteSide, TextureAtlasSprite defSpriteTrim) {
            this.backing = backing;
            this.delegate = new FramedModelInstance(FramedModelData.EMPTY);
            this.defSpriteFront = defSpriteFront;
            this.defSpriteSide = defSpriteSide;
            this.defSpriteTrim = defSpriteTrim;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemMapper;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            if (state instanceof IExtendedBlockState) {
                FramedModelData modelData = ((IExtendedBlockState)state).getValue(FramedModelData.PROP);
                if (modelData != null) {
                    return getFramedModelInstance(modelData).getQuads(state, side, rand);
                }
            }
            return super.getQuads(state, side, rand);
        }

        private IBakedModel getFramedModelInstance(FramedModelData modelData) {
            try {
                return instanceCache.get(modelData, () -> new FramedModelInstance(modelData));
            } catch (ExecutionException e) {
                return this; // can this even happen??
            }
        }

        private TextureAtlasSprite getStackSpriteOrDefault(ItemStack stack, TextureAtlasSprite defSprite) {
            if (stack.isEmpty()) {
                return defSprite;
            }
            TextureAtlasSprite icon = IconUtil.getIconFromStack(stack);
            return icon != null ? icon : defSprite;
        }

        private class FramedModelItemMapper extends ItemOverrideList {

            FramedModelItemMapper() {
                super(Collections.emptyList());
            }

            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                Item item = stack.getItem();
                if (item instanceof FramedItem) {
                    return getFramedModelInstance(new FramedModelData(((FramedItem)item).getMaterialData(stack)));
                }
                return BakedFramedTextureModel.this;
            }

        }

        private class FramedModelInstance extends DelegatingBakedModel {

            private final TextureAtlasSprite spriteFront, spriteSide, spriteTrim;
            @Nullable
            private final TextureAtlasSprite particleTexture;
            private final BakedQuadList quadsOpaque = new BakedQuadList(), quadsTrans = new BakedQuadList();
            private final BakedQuadList quadsAll = new BakedQuadList();

            public FramedModelInstance(FramedModelData modelData) {
                super(backing);
                this.spriteFront = getStackSpriteOrDefault(modelData.getEffectiveFrontMaterial(), defSpriteFront);
                this.spriteSide = getStackSpriteOrDefault(modelData.getEffectiveSideMaterial(), defSpriteSide);
                this.spriteTrim = getStackSpriteOrDefault(modelData.getEffectiveTrimMaterial(), defSpriteTrim);
                this.particleTexture = mapSprite(backing.getParticleTexture());
                for (BakedQuad quad : backing.getQuads(null, null, 0L)) {
                    processQuad(null, quad);
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    for (BakedQuad quad : backing.getQuads(null, face, 0L)) {
                        processQuad(face, quad);
                    }
                }
            }

            private void processQuad(@Nullable EnumFacing face, BakedQuad quad) {
                TextureAtlasSprite origSprite = quad.getSprite();
                TextureAtlasSprite mappedSprite = mapSprite(origSprite);
                if (mappedSprite == null) {
                    quadsOpaque.addQuad(face, quad);
                    quadsAll.addQuad(face, quad);
                } else {
                    BakedQuadRetextured mappedQuad = new BakedQuadRetextured(quad, mappedSprite);
                    if (origSprite == FramingTextureMapper.MARKER_SPRITE_FRONT) {
                        int[] vertexData = mappedQuad.getVertexData();
                        VertexFormat format = mappedQuad.getFormat();
                        int stride = format.getIntegerSize();
                        for (int i = format.getColorOffset() / 4; i < vertexData.length; i += stride) {
                            vertexData[i] = 0x7FFFFFFF;
                        }
                        quadsTrans.addQuad(face, mappedQuad);
                    } else {
                        quadsOpaque.addQuad(face, mappedQuad);
                    }
                    quadsAll.addQuad(face, mappedQuad);
                }
            }

            @Nullable
            private TextureAtlasSprite mapSprite(TextureAtlasSprite sprite) {
                if (sprite == FramingTextureMapper.MARKER_SPRITE_FRONT) { // 3 cmps probably isn't enough to justify a hash table
                    return spriteFront;
                } else if (sprite == FramingTextureMapper.MARKER_SPRITE_SIDE) {
                    return spriteSide;
                } else if (sprite == FramingTextureMapper.MARKER_SPRITE_TRIM) {
                    return spriteTrim;
                } else {
                    return null;
                }
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
                // we must have already mapped over the block state at this point
                BlockRenderLayer blockLayer = MinecraftForgeClient.getRenderLayer();
                if (blockLayer != null) {
                    switch (blockLayer) {
                        case CUTOUT:
                            return quadsOpaque.getQuads(side);
                        case TRANSLUCENT:
                            return quadsTrans.getQuads(side);
                        default:
                            return Collections.emptyList();
                    }
                } else {
                    return quadsAll.getQuads(side);
                }
            }

            @Override
            public TextureAtlasSprite getParticleTexture() {
                return particleTexture != null ? particleTexture : super.getParticleTexture();
            }

            @Override
            public ItemOverrideList getOverrides() {
                return itemMapper;
            }

        }

    }

}
