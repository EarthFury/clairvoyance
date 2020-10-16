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

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @see ClientConfig
 * @see DistanceFogVisibilityMode
 */
public class ClairvoyanceConfig {
	/**
	 * The client-side configuration for the mod. See {@link ModConfig.Type#CLIENT ModConfig.Type.CLIENT}
	 * for more information about what that is.
	 *
	 * @see #CLIENT_CONFIG_SPEC
	 */
	public static final ClientConfig CLIENT_CONFIG;

	/**
	 * The config spec for the client-side configuration for the mod
	 *
	 * @see #CLIENT_CONFIG
	 */
	public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ClairvoyanceMod.MODID);

	/**
	 * A default list of dimensions, used mostly as a suggestion
	 */
	private static final String[] DEFAULT_DISTANCE_FOG_DIMENSIONS = {"atum:atum"};

	// Static initialization for CLIENT_CONFIG & _SPEC
	static {
		final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_CONFIG_SPEC = specPair.getRight();
		CLIENT_CONFIG = specPair.getLeft();
	}

	/**
	 * The visibility mode for distance fog. This determines how we determine if distance fog should be rendered or not.
	 * ENABLED: Just always render it; The default option
	 * ENABLED_ONLY_IN: Only render it in the given list of dimensions
	 * ENABLED_EXCEPT_IN: Render it, except in the given list of dimensions
	 */
	public enum DistanceFogVisibilityMode {
		ENABLED_ONLY_IN, ENABLED_EXCEPT_IN, ENABLED;

		/**
		 * The real meat of determining if we render fog or not
		 *
		 * @param dimension The dimension of the player/screen for which we should maybe render fog for
		 * @return true if fog should be rendered; false if distance fog should not be rendered
		 */
		public boolean isVisibleFor(String dimension) {
			if(this == ENABLED) {
				return true;
			}

			final boolean isContained = CLIENT_CONFIG.distanceFogDimensionList.get().contains(dimension);
			switch(this) {
				case ENABLED_ONLY_IN:
					return isContained;
				case ENABLED_EXCEPT_IN:
					return !isContained;
			}
			// Unreachable
			return false;
		}
	}

	/**
	 * The main configuration for the mod, as well as two helper methods which represent the actual public API of the mod
	 */
	public static class ClientConfig {
		/**
		 * The visibility mode of the distance fog; used to determine if we show distance fog or not and how we filter by dimension
		 */
		public final ForgeConfigSpec.EnumValue<DistanceFogVisibilityMode> distanceFogVisibilityMode;

		/**
		 * The list of dimension names to which the visibility modes other than DistanceFogVisibilityMode.ENABLED apply
		 */
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> distanceFogDimensionList;

		/**
		 * If blindness fog should be displayed; Note that when disabled, it will disable all fog always when blind
		 */
		public final ForgeConfigSpec.BooleanValue isBlindnessFogEnabled;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			distanceFogVisibilityMode = builder
					.comment("The visibility mode of distance fog")
					.translation(ClairvoyanceMod.MODID + ".config." + "fogVisibility")
					.defineEnum("distanceFogVisibilityMode", DistanceFogVisibilityMode.ENABLED);
			distanceFogDimensionList = builder
					.comment("The list of dimension names to which distanceFogVisibilityMode applies to")
					.translation(ClairvoyanceMod.MODID + ".config." + "distanceFogDimensionList")
					.defineList("distanceFogDimensionList", Arrays.asList(DEFAULT_DISTANCE_FOG_DIMENSIONS), o -> true);
			isBlindnessFogEnabled = builder
					.comment("Should blindness fog be enabled (true), or fog totally disabled when blind? (false)")
					.translation(ClairvoyanceMod.MODID + ".config." + "isBlindnessFogEnabled")
					.define("isBlindnessFogEnabled", true);
		}
	}
}
