/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.glue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.effect.Effect;
import com.neophob.sematrix.core.effect.Effect.EffectName;
import com.neophob.sematrix.core.fader.IFader;
import com.neophob.sematrix.core.generator.Generator;
import com.neophob.sematrix.core.generator.Generator.GeneratorName;
import com.neophob.sematrix.core.listener.MessageProcessor;
import com.neophob.sematrix.core.properties.ValidCommands;
import com.neophob.sematrix.core.sound.BeatToAnimation;
import com.neophob.sematrix.core.sound.ISound;

/**
 * create random settings.
 *
 * @author michu
 */
public final class Shuffler {

	/** The log. */
	private static final Logger LOG = Logger.getLogger(Shuffler.class.getName());

	/**
	 * Instantiates a new shuffler.
	 */
	private Shuffler() {
		//no instance allowed
	}

	/**
	 * used for randomized preset mode, rarely change stuff.
	 */
	public static void randomPresentModeShuffler(ISound sound) {
		boolean kick = sound.isKick();
		boolean hat = sound.isHat();

		if (!kick && !hat) {
			return;
		}

		int fps = Collector.getInstance().getFps();

		Random rand = new Random();
		if (rand.nextInt(fps*3)==1) {
			
			LOG.log(Level.INFO, "Load random Preset");
			
			String[] msg = new String[1];
			msg[0] = ""+ValidCommands.PRESET_RANDOM;
			MessageProcessor.processMsg(msg, true, null);
		}
	}


	/**
	 * get a random and valid preset
	 */
	public static int getRandomPreset() {
		Collector col = Collector.getInstance();
		Random rand = new Random();

		LOG.log(Level.INFO, "Present Shuffler");

		int sanityCheck = 1000;
		boolean done=false;
		int idx = 0;
		while (!done || sanityCheck--<1) {
			idx = rand.nextInt(col.getPresets().size());
			List<String> present = col.getPresets().get(idx).getPresent();
			if (present!=null && present.size()>0) { 
				done = true;				
			}
		}
		return idx;
	}

	/**
	 * heavy shuffler! shuffle the current selected visual
	 * used by manual RANDOMIZE.
	 * 
	 */
	public static void manualShuffleStuff() {	
		long start = System.currentTimeMillis();

		Collector col = Collector.getInstance(); 		
		int currentVisual = col.getCurrentVisual();
		Visual visual = col.getVisual(currentVisual);
		Random rand = new Random();

		LOG.log(Level.INFO, "Manual Shuffle for Visual {0}", currentVisual);

		//optimize, update blinkenlighst movie file only when visible
		boolean isBlinkenLightsVisible = false;
		if (visual.getGenerator1Idx() == Generator.GeneratorName.BLINKENLIGHTS.getId() 
				|| visual.getGenerator2Idx() == Generator.GeneratorName.BLINKENLIGHTS.getId()) {
			isBlinkenLightsVisible = true;                
		}

		int totalNrGenerator = col.getPixelControllerGenerator().getSize();
		if (!col.getPixelControllerGenerator().isCaptureGeneratorActive()) {
			totalNrGenerator++;
		}
		int totalNrEffect = col.getPixelControllerEffect().getSize();
		int totalNrMixer = col.getPixelControllerMixer().getSize();

		if (col.getShufflerSelect(ShufflerOffset.GENERATOR_A)) {
			//make sure we only select inuse generators
			boolean isGeneratorInUse = false;
			while (!isGeneratorInUse) {
				//why -1 +1? the first effect is passthrough - so no effect
				visual.setGenerator1(rand.nextInt(totalNrGenerator-1)+1);
				isGeneratorInUse = visual.getGenerator1().isInUse();
			}		    
		}

		if (col.getShufflerSelect(ShufflerOffset.GENERATOR_B)) {			
			//make sure we only select inuse generators
			boolean isGeneratorInUse = false;
			while (!isGeneratorInUse) {
				//why -1 +1? the first effect is passthrough - so no effect
				visual.setGenerator2(rand.nextInt(totalNrGenerator-1)+1);
				isGeneratorInUse = visual.getGenerator2().isInUse();
			}           
		}

		if (col.getShufflerSelect(ShufflerOffset.EFFECT_A)) {
			visual.setEffect1(rand.nextInt(totalNrEffect));
		}

		if (col.getShufflerSelect(ShufflerOffset.EFFECT_B)) {
			visual.setEffect2(rand.nextInt(totalNrEffect));
		}

		if (col.getShufflerSelect(ShufflerOffset.MIXER)) {			
			if (visual.getGenerator2Idx()==0) {
				//no 2nd generator - use passthru mixer
				visual.setMixer(0);						
			} else {
				visual.setMixer(rand.nextInt(totalNrMixer));						
			}
		}

		//set used to find out if visual is on screen
		Set<Integer> activeGeneratorIds = new HashSet<Integer>();
		Set<Integer> activeEffectIds = new HashSet<Integer>();
		for (OutputMapping om: col.getAllOutputMappings()) {
			Visual v = col.getVisual(om.getVisualId());

			if (v.equals(visual)) {
				continue;
			}

			activeEffectIds.add(v.getEffect1Idx());
			activeEffectIds.add(v.getEffect2Idx());

			activeGeneratorIds.add(v.getGenerator1Idx());
			activeGeneratorIds.add(v.getGenerator2Idx());
		}

		//shuffle only items which are NOT visible
		for (Generator g: col.getPixelControllerGenerator().getAllGenerators()) {
			if (!activeGeneratorIds.contains(g.getId())) {

				//optimize, loading a blinkenlights movie file is quite expensive (takes a long time)
				//so load only a new movie file if the generator is active!
				if (g.getId() == Generator.GeneratorName.BLINKENLIGHTS.getId()) {
					if (isBlinkenLightsVisible) {
						g.shuffle();
					}
				} else {
					g.shuffle();   
				}				
			}
		}

		for (Effect e: col.getPixelControllerEffect().getAllEffects()) {
			if (!activeEffectIds.contains(e.getId())) {
				e.shuffle();
			}
		}

		//do not shuffle output
		/*if (col.getShufflerSelect(ShufflerOffset.OUTPUT)) {
			int nrOfVisuals = col.getAllVisuals().size();
			int screenNr = 0;
			for (OutputMapping om: col.getAllOutputMappings()) {
				Fader f=om.getFader();
				if (!f.isStarted()) {
					//start fader only if not another one is started
					f.startFade(rand.nextInt(nrOfVisuals), screenNr);
				}
				screenNr++;
			}
		}*/

		if (col.getShufflerSelect(ShufflerOffset.COLORSET)) {
			int colorSets = col.getColorSets().size();
			visual.setColorSet(rand.nextInt(colorSets));	
		}
		
		if (col.getShufflerSelect(ShufflerOffset.BEAT_WORK_MODE)) {
			BeatToAnimation bta = BeatToAnimation.values()[new Random().nextInt(BeatToAnimation.values().length)];
			col.getPixelControllerGenerator().setBta(bta);
		}
		
		if (col.getShufflerSelect(ShufflerOffset.GENERATORSPEED)) {
			col.getPixelControllerGenerator().setFpsAdjustment(new Random().nextFloat()*2.0f);
		}

		LOG.log(Level.INFO, "Shuffle finished in {0}ms", (System.currentTimeMillis()-start));
	}

	/**
	 * used for randomized mode, rarely change stuff.
	 */
	public static void shuffleStuff(ISound sound) {
		boolean kick = sound.isKick();
		boolean hat = sound.isHat();
		boolean snare = sound.isSnare();

		if (!hat && !kick && !snare) {
			return;
		}

		Collector col = Collector.getInstance(); 

		Random rand = new Random();
		int blah = rand.nextInt(18);
		//LOG.log(Level.INFO, "Automatic Shuffler {0}", blah);

		if (snare) {			
			if (blah == 1 && col.getShufflerSelect(ShufflerOffset.GENERATOR_A)) {
				int size = col.getPixelControllerGenerator().getSize();
				for (Visual v: col.getAllVisuals()) {
					v.setGenerator1(rand.nextInt(size-1)+1);
				}
			}

			if (blah == 2 && col.getShufflerSelect(ShufflerOffset.GENERATOR_B)) {
				int size = col.getPixelControllerGenerator().getSize();
				for (Visual v: col.getAllVisuals()) {
					v.setGenerator2(rand.nextInt(size));
				}
			}

			if (blah == 3 && col.getShufflerSelect(ShufflerOffset.EFFECT_A)) {
				int size = col.getPixelControllerEffect().getSize();
				for (Visual v: col.getAllVisuals()) {
					v.setEffect1(rand.nextInt(size));
				}
			}

			if (blah == 4 && col.getShufflerSelect(ShufflerOffset.EFFECT_B)) {
				int size = col.getPixelControllerEffect().getSize();
				for (Visual v: col.getAllVisuals()) {
					v.setEffect2(rand.nextInt(size));
				}
			}
			
			if (blah == 5 && col.getShufflerSelect(ShufflerOffset.COLORSET)) {
				int colorSets = col.getColorSets().size();
				for (Visual v: col.getAllVisuals()) {
					v.setColorSet(rand.nextInt(colorSets));
				}				
			}

			if (blah == 6) {
				col.getPixelControllerEffect().getEffect(EffectName.THRESHOLD).shuffle();
			}

		}

		if (hat) {
			if (blah == 7 && col.getShufflerSelect(ShufflerOffset.MIXER)) {
				int size = col.getPixelControllerMixer().getSize();
				for (Visual v: col.getAllVisuals()) {
					if (v.getGenerator2Idx()==0) {
						//no 2nd generator - use passthru mixer
						v.setMixer(0);						
					} else {
						v.setMixer(rand.nextInt(size));						
					}
				}
			}			

			if (blah == 8 && col.getShufflerSelect(ShufflerOffset.FADER_OUTPUT)) {
				int size = col.getPixelControllerFader().getFaderCount();
				for (OutputMapping om: col.getAllOutputMappings()) {
					IFader f=om.getFader();
					if (!f.isStarted()) {
						om.setFader(
								col.getPixelControllerFader().getVisualFader(rand.nextInt(size)));	
					}
				}
			}


			if (blah == 9) {
				col.getPixelControllerEffect().getEffect(EffectName.ROTOZOOM).shuffle();
			}

			if (blah == 10) {
				col.getPixelControllerEffect().getEffect(EffectName.ZOOM).shuffle();
			}

			if (blah == 11 && col.getShufflerSelect(ShufflerOffset.GENERATORSPEED)) {
				col.getPixelControllerGenerator().setFpsAdjustment(new Random().nextFloat()*2.0f);
			}

			if (blah == 12 && col.getShufflerSelect(ShufflerOffset.BEAT_WORK_MODE)) {
				BeatToAnimation bta = BeatToAnimation.values()[new Random().nextInt(BeatToAnimation.values().length)];
				col.getPixelControllerGenerator().setBta(bta);
			}
			
		}


		if (kick) {
			if (blah == 13 && col.getShufflerSelect(ShufflerOffset.OUTPUT)) {
				int nrOfVisuals = col.getAllVisuals().size();
				int screenNr = 0;
				for (OutputMapping om: col.getAllOutputMappings()) {
					IFader f=om.getFader();
					if (!f.isStarted()) {
						//start fader only if not another one is started
						f.startFade(rand.nextInt(nrOfVisuals), screenNr);
					}
					screenNr++;
				}
			}

			if (blah == 14) {
				col.getPixelControllerGenerator().getGenerator(GeneratorName.IMAGE).shuffle();
			}

			if (blah == 15) {
				col.getPixelControllerGenerator().getGenerator(GeneratorName.BLINKENLIGHTS).shuffle();
			}

			if (blah == 16) {
				col.getPixelControllerEffect().getEffect(EffectName.TEXTURE_DEFORMATION).shuffle();
			}

			if (blah == 17) {
				col.getPixelControllerGenerator().getGenerator(GeneratorName.COLOR_SCROLL).shuffle();
			}


		}

	}
}
