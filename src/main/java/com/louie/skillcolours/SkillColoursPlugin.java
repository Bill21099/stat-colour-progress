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
 *    and/or technical materials provided with the distribution.
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

import com.google.inject.Provides;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Skill Colours Plugin
 *
 * <p>Dynamically colours skill level text in the Skills tab based on the player's actual
 * (base/real) skill level, providing an at-a-glance visual indication of progression towards 99.
 *
 * <h3>Colour bands</h3>
 * <ul>
 *   <li>1–54   : Red
 *   <li>55–91  : Yellow → Orange (smooth linear RGB interpolation)
 *   <li>92–98  : Orange → Green (smooth linear RGB interpolation)
 *   <li>99     : Gold + subtly dimmed skill entry
 * </ul>
 */
@Slf4j
@PluginDescriptor(
	name = "Skill Colours",
	description = "Colours skill level text in the Skills tab based on your level (red → yellow → orange → green → gold at 99)",
	tags = {"skills", "stats", "colour", "color", "level", "99", "progression"}
)
public class SkillColoursPlugin extends Plugin
{
	/** Interface group ID for the Stats tab in OSRS. */
	static final int STATS_INTERFACE_ID = 320;

	/** Default widget opacity (fully opaque). */
	static final int OPACITY_NORMAL = 0;

	/** Opacity applied to 99 skill entries — approximately 20% dimmer. */
	static final int OPACITY_99 = 50; // 0–255 scale; 50 ≈ 20% dim

	/**
	 * Canonical mapping of OSRS Stats interface (320) child IDs to Skills.
	 * Standard layout (3 columns x 8 rows):
	 * Col 1: Attack(1), Strength(2), Defence(3), Ranged(4), Prayer(5), Magic(6), Runecraft(7), Construction(8)
	 * Col 2: Hitpoints(9), Agility(10), Herblore(11), Thieving(12), Crafting(13), Fletching(14), Slayer(15), Hunter(16)
	 * Col 3: Mining(17), Smithing(18), Fishing(19), Cooking(20), Firemaking(21), Woodcutting(22), Farming(23), Sailing(24)
	 */
	private static final Skill[] STATS_CHILD_SKILLS = new Skill[] {
		null, // child 0 is container / header
		Skill.ATTACK,        // child 1
		Skill.STRENGTH,      // child 2
		Skill.DEFENCE,       // child 3
		Skill.RANGED,        // child 4
		Skill.PRAYER,        // child 5
		Skill.MAGIC,         // child 6
		Skill.RUNECRAFT,     // child 7
		Skill.CONSTRUCTION,  // child 8
		Skill.HITPOINTS,     // child 9
		Skill.AGILITY,       // child 10
		Skill.HERBLORE,      // child 11
		Skill.THIEVING,      // child 12
		Skill.CRAFTING,      // child 13
		Skill.FLETCHING,     // child 14
		Skill.SLAYER,        // child 15
		Skill.HUNTER,        // child 16
		Skill.MINING,        // child 17
		Skill.SMITHING,      // child 18
		Skill.FISHING,       // child 19
		Skill.COOKING,       // child 20
		Skill.FIREMAKING,    // child 21
		Skill.WOODCUTTING,   // child 22
		Skill.FARMING,       // child 23
		Skill.SAILING        // child 24
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SkillColoursConfig config;

	@Override
	protected void startUp()
	{
		clientThread.invokeLater(this::updateSkillColours);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::restoreAllSkills);
	}

	@Provides
	SkillColoursConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillColoursConfig.class);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		updateSkillColours();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == STATS_INTERFACE_ID)
		{
			updateSkillColours();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			updateSkillColours();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(this::updateSkillColours);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING
			|| event.getGameState() == GameState.CONNECTION_LOST)
		{
			// State reset on logout
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(SkillColoursConfig.GROUP))
		{
			return;
		}
		clientThread.invokeLater(this::updateSkillColours);
	}

	/**
	 * Scans the Stats interface and applies text colouring and dimming.
	 */
	private void updateSkillColours()
	{
		final Widget statsContainer = client.getWidget(ComponentID.SKILLS_CONTAINER);
		if (statsContainer == null || statsContainer.isHidden())
		{
			return;
		}

		// Process each standard skill slot in Interface 320
		for (int childId = 1; childId < STATS_CHILD_SKILLS.length; childId++)
		{
			final Skill mappedSkill = STATS_CHILD_SKILLS[childId];
			if (mappedSkill == null)
			{
				continue;
			}

			final Widget skillWidget = client.getWidget(STATS_INTERFACE_ID, childId);
			if (skillWidget == null || skillWidget.isHidden())
			{
				continue;
			}

			// Validate or determine skill from widget metadata (fallback to mappedSkill)
			final Skill skill = identifySkill(skillWidget, mappedSkill);
			final int realLevel = client.getRealSkillLevel(skill);

			final int targetColour = config.enableColourProgression()
				? SkillColourCalculator.colourForLevel(
					realLevel,
					config.level1to45Colour(),
					config.level46to65Colour(),
					config.level66to89Colour(),
					config.level90to98Colour(),
					config.level99Colour()
				)
				: SkillColourCalculator.DEFAULT_TEXT_COLOUR;

			final int targetOpacity = (config.enable99Dimming() && realLevel >= 99)
				? OPACITY_99
				: OPACITY_NORMAL;

			// Apply opacity to the skill container entry
			if (skillWidget.getOpacity() != targetOpacity)
			{
				skillWidget.setOpacity(targetOpacity);
			}

			// Apply colour to all text sub-widgets (current level & base level)
			recolourTextChildren(skillWidget, targetColour);
		}
	}

	/**
	 * Recursively finds and recolours all text children within a skill entry widget.
	 */
	private void recolourTextChildren(Widget widget, int targetColour)
	{
		if (widget == null)
		{
			return;
		}

		// If this widget has level text, update its colour
		final String text = widget.getText();
		if (text != null && !text.isEmpty())
		{
			// Don't modify the total level text
			if (!text.toLowerCase().contains("total"))
			{
				if (widget.getTextColor() != targetColour)
				{
					widget.setTextColor(targetColour);
				}
			}
		}

		// Traverse dynamic, static, and nested children
		final Widget[] dynamicChildren = widget.getDynamicChildren();
		if (dynamicChildren != null)
		{
			for (Widget child : dynamicChildren)
			{
				recolourTextChildren(child, targetColour);
			}
		}

		final Widget[] staticChildren = widget.getStaticChildren();
		if (staticChildren != null)
		{
			for (Widget child : staticChildren)
			{
				recolourTextChildren(child, targetColour);
			}
		}

		final Widget[] nestedChildren = widget.getNestedChildren();
		if (nestedChildren != null)
		{
			for (Widget child : nestedChildren)
			{
				recolourTextChildren(child, targetColour);
			}
		}
	}

	/**
	 * Determines the Skill for a given widget using its action/name metadata,
	 * falling back to the default mapped skill.
	 */
	private Skill identifySkill(Widget widget, Skill defaultSkill)
	{
		// Check widget actions (e.g. "View Attack guide")
		final String[] actions = widget.getActions();
		if (actions != null)
		{
			for (String action : actions)
			{
				if (action == null)
				{
					continue;
				}
				final Skill matched = matchSkillName(action);
				if (matched != null)
				{
					return matched;
				}
			}
		}

		// Check widget name (e.g. "<col=ff9040>Attack</col>")
		final String name = widget.getName();
		if (name != null)
		{
			final Skill matched = matchSkillName(name);
			if (matched != null)
			{
				return matched;
			}
		}

		return defaultSkill;
	}

	private Skill matchSkillName(String text)
	{
		final String lower = text.toLowerCase();
		for (Skill s : Skill.values())
		{
			if (s == Skill.OVERALL)
			{
				continue;
			}
			if (lower.contains(s.getName().toLowerCase()))
			{
				return s;
			}
		}
		return null;
	}

	/**
	 * Restores all skill widgets to the default OSRS yellow colour and normal opacity.
	 */
	private void restoreAllSkills()
	{
		for (int childId = 1; childId < STATS_CHILD_SKILLS.length; childId++)
		{
			final Widget skillWidget = client.getWidget(STATS_INTERFACE_ID, childId);
			if (skillWidget != null)
			{
				skillWidget.setOpacity(OPACITY_NORMAL);
				recolourTextChildren(skillWidget, SkillColourCalculator.DEFAULT_TEXT_COLOUR);
			}
		}
	}
}
