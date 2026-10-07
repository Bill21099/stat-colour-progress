/*
 * Copyright (c) 2024, Louie
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.louie.skillcolours;

import java.awt.Color;

/**
 * Pure colour-calculation logic for the Skill Colours plugin.
 *
 * <h3>Colour progression</h3>
 * <pre>
 * Level  1–45  : Red    (#CC2200)
 * Level 46–65  : Red    (#CC2200) → Orange (#FF7000)   [linear RGB interpolation]
 * Level 66–89  : Orange (#FF7000) → Yellow (#FFFF00)   [linear RGB interpolation]
 * Level 90–98  : Yellow (#FFFF00) → Green  (#00CC44)   [linear RGB interpolation]
 * Level 99     : Gold   (#FFD700)
 * </pre>
 */
public final class SkillColourCalculator
{
	// -------------------------------------------------------------------------
	// Default colour constants (packed RGB24)
	// -------------------------------------------------------------------------

	/** OSRS-style red used for levels 1–45. */
	static final int COLOUR_RED    = 0xCC2200;

	/** Orange reached at level 65. */
	static final int COLOUR_ORANGE = 0xFF7000;

	/** Yellow reached at level 89. */
	static final int COLOUR_YELLOW = 0xFFFF00;

	/** Strong green reached at level 98 (close to max). */
	static final int COLOUR_GREEN  = 0x00CC44;

	/** Gold used exclusively at level 99. */
	static final int COLOUR_GOLD   = 0xFFD700;

	/** Default OSRS skill-level text colour (bright yellow). */
	static final int DEFAULT_TEXT_COLOUR = 0xFFFF00;

	private SkillColourCalculator()
	{
		// Utility class
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Returns the packed RGB24 colour for the given skill level using default colour anchors.
	 *
	 * @param level the player's real (base) skill level, clamped to [1, 99]
	 * @return packed RGB colour suitable for {@code Widget.setTextColor}
	 */
	public static int colourForLevel(int level)
	{
		return colourForLevel(level, COLOUR_RED, COLOUR_ORANGE, COLOUR_YELLOW, COLOUR_GREEN, COLOUR_GOLD);
	}

	/**
	 * Returns the packed RGB24 colour for the given skill level using custom {@link Color} anchors.
	 */
	public static int colourForLevel(int level, Color cRed, Color cOrange, Color cYellow, Color cGreen, Color cGold)
	{
		final int redRGB = cRed != null ? (cRed.getRGB() & 0xFFFFFF) : COLOUR_RED;
		final int orangeRGB = cOrange != null ? (cOrange.getRGB() & 0xFFFFFF) : COLOUR_ORANGE;
		final int yellowRGB = cYellow != null ? (cYellow.getRGB() & 0xFFFFFF) : COLOUR_YELLOW;
		final int greenRGB = cGreen != null ? (cGreen.getRGB() & 0xFFFFFF) : COLOUR_GREEN;
		final int goldRGB = cGold != null ? (cGold.getRGB() & 0xFFFFFF) : COLOUR_GOLD;

		return colourForLevel(level, redRGB, orangeRGB, yellowRGB, greenRGB, goldRGB);
	}

	/**
	 * Returns the packed RGB24 colour for the given skill level with custom RGB anchors.
	 *
	 * @param level the player's real (base) skill level, clamped to [1, 99]
	 * @param cRed colour for levels 1–45 (and start of 46–65 ramp)
	 * @param cOrange target colour at level 65 (and start of 66–89 yellow ramp)
	 * @param cYellow target colour at level 89 (and start of 90–98 green ramp)
	 * @param cGreen target colour at level 98
	 * @param cGold colour for level 99
	 * @return packed RGB colour suitable for {@code Widget.setTextColor}
	 */
	public static int colourForLevel(int level, int cRed, int cOrange, int cYellow, int cGreen, int cGold)
	{
		level = Math.max(1, Math.min(99, level));

		if (level >= 99)
		{
			return cGold;
		}
		else if (level >= 90)
		{
			// 90 → 98: Yellow to Green
			// t = 0 at level 89, t = 1 at level 98
			final float t = (level - 89) / 9.0f;
			return lerpRGB(cYellow, cGreen, t);
		}
		else if (level >= 66)
		{
			// 66 → 89: Orange to Yellow
			// t = 0 at level 65, t = 1 at level 89
			final float t = (level - 65) / 24.0f;
			return lerpRGB(cOrange, cYellow, t);
		}
		else if (level >= 46)
		{
			// 46 → 65: Red to Orange
			// t = 0 at level 45, t = 1 at level 65
			final float t = (level - 45) / 20.0f;
			return lerpRGB(cRed, cOrange, t);
		}
		else
		{
			// 1 → 45: Red
			return cRed;
		}
	}

	// -------------------------------------------------------------------------
	// Interpolation helpers
	// -------------------------------------------------------------------------

	/**
	 * Linearly interpolates between two packed RGB24 colours.
	 *
	 * @param from packed RGB24 start colour
	 * @param to   packed RGB24 end colour
	 * @param t    interpolation factor in [0, 1]
	 * @return interpolated packed RGB24 colour
	 */
	static int lerpRGB(int from, int to, float t)
	{
		t = Math.max(0f, Math.min(1f, t));

		final int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
		final int g = lerpChannel((from >>  8) & 0xFF, (to >>  8) & 0xFF, t);
		final int b = lerpChannel( from        & 0xFF,  to        & 0xFF, t);

		return (r << 16) | (g << 8) | b;
	}

	/**
	 * Linearly interpolates a single 8-bit colour channel.
	 *
	 * @param from start value (0–255)
	 * @param to   end value (0–255)
	 * @param t    interpolation factor in [0, 1]
	 * @return interpolated value, clamped to [0, 255]
	 */
	static int lerpChannel(int from, int to, float t)
	{
		return Math.round(from + (to - from) * t);
	}
}
