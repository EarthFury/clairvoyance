////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2020 Earth-Fury
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
////////////////////////////////////////////////////////////////////////////////

package net.shados.earthfury.minecraft.clairvoyance;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod("clairvoyance")
public class ClairvoyanceMod
{
    private static final Logger LOGGER = LogManager.getLogger(ClairvoyanceMod.MODID);
    public static final String MODID = "clairvoyance";

    public ClairvoyanceMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClairvoyanceConfig.CLIENT_CONFIG_SPEC);
    }

    @EventBusSubscriber({Dist.CLIENT})
    @SuppressWarnings("unused")
    private static class Handler {
        /**
         * All we really do here is validate the dimension list, and dispatch warnings for dimensions that don't exist.
         * It's just a warning because a dimension may come to exist under a known name later. It should be dynamically
         * included, which is why we still only store the strings. It's better for us to calculate it each time than
         * make presumptions.
         */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void handleFMLCommonSetupEvent(FMLCommonSetupEvent event) {
            List<DimensionType> dimensions = new ArrayList<>();
            DimensionType.getAll().forEach(dimensions::add);

            // Fail early if our config is just borked to hell somehow by referencing it in init.
            LOGGER.info("CLIENT_CONFIG.distanceFogVisibilityMode mode set to " + ClairvoyanceConfig.CLIENT_CONFIG.distanceFogVisibilityMode.get().toString());
            LOGGER.info("Validating CLIENT_CONFIG.distanceFogDimensionList ...");
            for(String dimension : ClairvoyanceConfig.CLIENT_CONFIG.distanceFogDimensionList.get()) {
                if(dimensions.contains(dimension)) {
                    LOGGER.info("Valid dimension in distanceFogDimensionList: " + dimension);
                } else {
                    LOGGER.warn("Invalid dimension name in distanceFogDimensionList: " + dimension + ", it will be ignored until such a dimension exists");
                }
            }
        }
        /**
         * Here is where we modify fog's visibility. We zero the density on every FogDensity event.
         *
         * @param event the render event for fog density
         * @see EntityViewRenderEvent.FogDensity
         */
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void handleFogDensityEvent(EntityViewRenderEvent.FogDensity event) {
            final Entity entity = Minecraft.getInstance().getRenderViewEntity();
            if(entity == null) {
                LOGGER.error("Minecraft.getRenderViewEntity() returned null in a rendering event ??? uhh error code... contact me on github?");
                return;
            }

            final String currentDimension;
            try {
                currentDimension = Objects.requireNonNull(entity.dimension.getRegistryName()).toString();
            } catch(NullPointerException e) {
                LOGGER.error("Dimension the RenderViewEntity exists in has a null dimension.registryName", e);
                return;
            }

            // Skip rendering fog by zeroing the density and cancelling the event if we should
            if(!ClairvoyanceConfig.CLIENT_CONFIG.distanceFogVisibilityMode.get().isVisibleFor(currentDimension)) {
                event.setDensity(0.0F);
                event.setCanceled(true);
            }
        }
    }
}
