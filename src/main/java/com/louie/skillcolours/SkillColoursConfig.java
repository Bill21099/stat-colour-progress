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
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SkillColoursConfig.GROUP)
public interface SkillColoursConfig extends Config
{
	String GROUP = "skillcolours";

	@ConfigItem(
		keyName = "enableColourProgression",
		name = "Enable colour progression",
		description = "Colour skill level text based on your level",
		position = 0
	)
	default boolean enableColourProgression()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enable99Dimming",
		name = "Dim completed (99) skills",
		description = "Slightly dims the skill entry for any skill you have reached level 99 in",
		position = 1
	)
	default boolean enable99Dimming()
	{
		return true;
	}

	@ConfigSection(
		name = "Colours",
		description = "Customise progression and level 99 colours",
		position = 2
	)
	String coloursSection = "coloursSection";

	@ConfigItem(
		keyName = "level1to45Colour",
		name = "Level 1–45 (Red)",
		description = "Base colour for levels 1–45",
		section = "coloursSection",
		position = 10
	)
	default Color level1to45Colour()
	{
		return new Color(0xCC, 0x22, 0x00);
	}

	@ConfigItem(
		keyName = "level46to65Colour",
		name = "Level 46–65 (Orange)",
		description = "Target colour reached at level 65 (transitioning from level 45)",
		section = "coloursSection",
		position = 11
	)
	default Color level46to65Colour()
	{
		return new Color(0xFF, 0x70, 0x00);
	}

	@ConfigItem(
		keyName = "level66to89Colour",
		name = "Level 66–89 (Yellow)",
		description = "Target colour reached at level 89 (transitioning from level 65)",
		section = "coloursSection",
		position = 12
	)
	default Color level66to89Colour()
	{
		return new Color(0xFF, 0xFF, 0x00);
	}

	@ConfigItem(
		keyName = "level90to98Colour",
		name = "Level 90–98 (Green)",
		description = "Target colour reached at level 98 (transitioning from level 89)",
		section = "coloursSection",
		position = 13
	)
	default Color level90to98Colour()
	{
		return new Color(0x00, 0xCC, 0x44);
	}

	@ConfigItem(
		keyName = "level99Colour",
		name = "Level 99 (Gold)",
		description = "Colour used exclusively for level 99 skills",
		section = "coloursSection",
		position = 14
	)
	default Color level99Colour()
	{
		return new Color(0xFF, 0xD7, 0x00);
	}
}
