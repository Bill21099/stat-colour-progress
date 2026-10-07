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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link SkillColourCalculator}.
 */
public class SkillColourCalculatorTest
{
	// -------------------------------------------------------------------------
	// Boundary values
	// -------------------------------------------------------------------------

	@Test
	public void testLevel1IsRed()
	{
		assertEquals(SkillColourCalculator.COLOUR_RED, SkillColourCalculator.colourForLevel(1));
	}

	@Test
	public void testLevel45IsRed()
	{
		assertEquals(SkillColourCalculator.COLOUR_RED, SkillColourCalculator.colourForLevel(45));
	}

	@Test
	public void testLevel65IsOrange()
	{
		assertEquals(SkillColourCalculator.COLOUR_ORANGE, SkillColourCalculator.colourForLevel(65));
	}

	@Test
	public void testLevel89IsYellow()
	{
		assertEquals(SkillColourCalculator.COLOUR_YELLOW, SkillColourCalculator.colourForLevel(89));
	}

	@Test
	public void testLevel98IsGreen()
	{
		assertEquals(SkillColourCalculator.COLOUR_GREEN, SkillColourCalculator.colourForLevel(98));
	}

	@Test
	public void testLevel99IsGold()
	{
		assertEquals(SkillColourCalculator.COLOUR_GOLD, SkillColourCalculator.colourForLevel(99));
	}

	// -------------------------------------------------------------------------
	// Clamping
	// -------------------------------------------------------------------------

	@Test
	public void testLevelBelowOneClampedToRed()
	{
		assertEquals(SkillColourCalculator.COLOUR_RED, SkillColourCalculator.colourForLevel(0));
		assertEquals(SkillColourCalculator.COLOUR_RED, SkillColourCalculator.colourForLevel(-1));
	}

	@Test
	public void testLevelAbove99ClampedToGold()
	{
		assertEquals(SkillColourCalculator.COLOUR_GOLD, SkillColourCalculator.colourForLevel(100));
		assertEquals(SkillColourCalculator.COLOUR_GOLD, SkillColourCalculator.colourForLevel(126));
	}

	// -------------------------------------------------------------------------
	// Interpolation correctness
	// -------------------------------------------------------------------------

	@Test
	public void testLevel55IsMidwayRedToOrange()
	{
		// Level 55 is midpoint of [45, 65] -> t = 10/20 = 0.5
		int expected = SkillColourCalculator.lerpRGB(
			SkillColourCalculator.COLOUR_RED,
			SkillColourCalculator.COLOUR_ORANGE,
			0.5f);
		assertEquals(expected, SkillColourCalculator.colourForLevel(55));
	}

	@Test
	public void testLevel77IsMidwayOrangeToYellow()
	{
		// Level 77 is midpoint of [65, 89] -> t = 12/24 = 0.5
		int expected = SkillColourCalculator.lerpRGB(
			SkillColourCalculator.COLOUR_ORANGE,
			SkillColourCalculator.COLOUR_YELLOW,
			0.5f);
		assertEquals(expected, SkillColourCalculator.colourForLevel(77));
	}

	@Test
	public void testLevel94IsMidwayYellowToGreen()
	{
		// Level 94: (94-89)/9 ≈ 5/9
		int expected = SkillColourCalculator.lerpRGB(
			SkillColourCalculator.COLOUR_YELLOW,
			SkillColourCalculator.COLOUR_GREEN,
			(94 - 89) / 9.0f);
		assertEquals(expected, SkillColourCalculator.colourForLevel(94));
	}

	// -------------------------------------------------------------------------
	// Interpolation helpers
	// -------------------------------------------------------------------------

	@Test
	public void testLerpChannelZeroT()
	{
		assertEquals(100, SkillColourCalculator.lerpChannel(100, 200, 0f));
	}

	@Test
	public void testLerpChannelOneT()
	{
		assertEquals(200, SkillColourCalculator.lerpChannel(100, 200, 1f));
	}

	@Test
	public void testLerpChannelHalfT()
	{
		assertEquals(150, SkillColourCalculator.lerpChannel(100, 200, 0.5f));
	}

	@Test
	public void testLerpRGBIdentity()
	{
		final int colour = 0xABCDEF;
		assertEquals(colour, SkillColourCalculator.lerpRGB(colour, colour, 0.5f));
	}

	// -------------------------------------------------------------------------
	// Custom Colours
	// -------------------------------------------------------------------------

	@Test
	public void testCustomColours()
	{
		Color cRed = Color.BLUE;
		Color cOrange = Color.WHITE;
		Color cYellow = Color.PINK;
		Color cGreen = Color.CYAN;
		Color cGold = Color.MAGENTA;

		assertEquals(Color.BLUE.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(1, cRed, cOrange, cYellow, cGreen, cGold));
		assertEquals(Color.BLUE.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(45, cRed, cOrange, cYellow, cGreen, cGold));
		assertEquals(Color.WHITE.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(65, cRed, cOrange, cYellow, cGreen, cGold));
		assertEquals(Color.PINK.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(89, cRed, cOrange, cYellow, cGreen, cGold));
		assertEquals(Color.CYAN.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(98, cRed, cOrange, cYellow, cGreen, cGold));
		assertEquals(Color.MAGENTA.getRGB() & 0xFFFFFF, SkillColourCalculator.colourForLevel(99, cRed, cOrange, cYellow, cGreen, cGold));
	}

	// -------------------------------------------------------------------------
	// No null / no exception across full range
	// -------------------------------------------------------------------------

	@Test
	public void testAllLevelsReturnNonNegativeColour()
	{
		for (int level = 1; level <= 99; level++)
		{
			final int colour = SkillColourCalculator.colourForLevel(level);
			assertTrue("Colour should be >= 0 at level " + level, colour >= 0);
			assertTrue("Colour should fit in 24 bits at level " + level, colour <= 0xFFFFFF);
		}
	}
}
