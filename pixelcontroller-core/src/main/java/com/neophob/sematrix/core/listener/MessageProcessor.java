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
package com.neophob.sematrix.core.listener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.effect.Effect;
import com.neophob.sematrix.core.effect.RotoZoom;
import com.neophob.sematrix.core.effect.Effect.EffectName;
import com.neophob.sematrix.core.fader.IFader;
import com.neophob.sematrix.core.fader.TransitionManager;
import com.neophob.sematrix.core.generator.Generator;
import com.neophob.sematrix.core.glue.Collector;
import com.neophob.sematrix.core.glue.OutputMapping;
import com.neophob.sematrix.core.glue.Shuffler;
import com.neophob.sematrix.core.mixer.Mixer;
import com.neophob.sematrix.core.properties.ValidCommands;
import com.neophob.sematrix.core.sound.BeatToAnimation;

/**
 * The Class MessageProcessor.
 */
public final class MessageProcessor {


	/** The log. */
	private static final Logger LOG = Logger.getLogger(MessageProcessor.class.getName());

	/** The Constant IGNORE_COMMAND. */
	private static final String IGNORE_COMMAND = "Ignored command";

	/**
	 * Instantiates a new message processor.
	 */
	private MessageProcessor() {
		//no instance
	}

	/**
	 * process message from gui.
	 *
	 * @param msg the msg
	 * @param startFader the start fader
	 * @return STATUS if we need to send updates back to the gui (loaded preferences)
	 */
	public static synchronized void processMsg(String[] msg, boolean startFader, byte[] blob) {
		if (msg==null || msg.length<1) {
			return;
		}
                
		int msgLength = msg.length-1;
		int tmp;		
		try {
			ValidCommands cmd = ValidCommands.valueOf(msg[0]);
			Collector col = Collector.getInstance();
			switch (cmd) {
			case CHANGE_GENERATOR_A:
				try {
					int nr = col.getCurrentVisual();
					tmp=Integer.parseInt(msg[1]);
					Generator g = col.getPixelControllerGenerator().getGenerator(tmp);
					//silly check of generator exists
					g.getId();
					col.getVisual(nr).setGenerator1(g);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_GENERATOR_B:
				try {
					//the new method - used by the gui
					int nr = col.getCurrentVisual();
					tmp=Integer.parseInt(msg[1]);
					Generator g = col.getPixelControllerGenerator().getGenerator(tmp);
					g.getId();
					col.getVisual(nr).setGenerator2(g);
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_EFFECT_A:
				try {
					int nr = col.getCurrentVisual();
					tmp=Integer.parseInt(msg[1]);
					Effect e = col.getPixelControllerEffect().getEffect(tmp);
					e.getId();
					col.getVisual(nr).setEffect1(e);						
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_EFFECT_B:
				try {
					int nr = col.getCurrentVisual();
					tmp=Integer.parseInt(msg[1]);
					Effect e = col.getPixelControllerEffect().getEffect(tmp);
					e.getId();
					col.getVisual(nr).setEffect2(e);						
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_MIXER:
				try {
					//the new method - used by the gui
					int nr = col.getCurrentVisual();
					tmp=Integer.parseInt(msg[1]);
					Mixer m = col.getPixelControllerMixer().getMixer(tmp);
					m.getId();
					col.getVisual(nr).setMixer(m);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_OUTPUT_VISUAL:
				try {
					int nr = col.getCurrentOutput();				
					int newFx = Integer.parseInt(msg[1]);
					int oldFx = col.getFxInputForScreen(nr);
					int nrOfVisual = col.getAllVisuals().size();
					LOG.log(Level.INFO,	"old fx: {0}, new fx {1}", new Object[] {oldFx, newFx});
					if (oldFx!=newFx && newFx>=0 && newFx<nrOfVisual) {
						LOG.log(Level.INFO,	"Change Output 0, old fx: {0}, new fx {1}", new Object[] {oldFx, newFx});
						if (startFader) {
							//start fader to change screen
							col.getOutputMappings(nr).getFader().startFade(newFx, nr);								
						} else {
							//do not fade if we load setting from present
							col.mapInputToScreen(nr, newFx);
						}
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_ALL_OUTPUT_VISUAL:
				try {
				    int newFx = Integer.parseInt(msg[1]);
					int size = col.getAllOutputMappings().size();					
					int nrOfVisual = col.getAllVisuals().size();
					
					if (newFx>=0 && newFx<nrOfVisual) {
	                    for (int i=0; i<size; i++) {                        
	                        int oldFx = col.getFxInputForScreen(i);                     
	                        if (oldFx!=newFx) {
	                            LOG.log(Level.INFO, "Change Output 0, old fx: {0}, new fx {1}", new Object[] {oldFx, newFx});
	                            if (startFader) {
	                                //start fader to change screen
	                                col.getOutputMappings(i).getFader().startFade(newFx, i);                                
	                            } else {
	                                //do not fade if we load setting from present
	                                col.mapInputToScreen(i, newFx);
	                            }
	                        }                       
	                    }					    
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_OUTPUT_FADER:
				try {
					int nr = col.getCurrentOutput();
					tmp=Integer.parseInt(msg[1]);
					//do not start a new fader while the old one is still running
					if (!col.getOutputMappings(nr).getFader().isStarted()) {
					    IFader f = col.getPixelControllerFader().getVisualFader(tmp);
					    if (f!=null) {
					        col.getOutputMappings(nr).setFader(f);   
					    }
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_ALL_OUTPUT_FADER:
				try {
					tmp=Integer.parseInt(msg[1]);
					for (OutputMapping om: col.getAllOutputMappings()) {
						//do not start a new fader while the old one is still running
						if (!om.getFader().isStarted()) {
						    IFader f = col.getPixelControllerFader().getVisualFader(tmp);
						    if (f!=null) {
						        om.setFader(f);						        
						    }
						}						
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_SHUFFLER_SELECT:
				try {					
					int size = col.getPixelControllerShufflerSelect().getShufflerSelect().size();
					if (size>msgLength) {
						size=msgLength;
					}
					boolean b;
					String str="";
					for (int i=0; i<size; i++) {
						b = false;
						if (msg[i+1].equals("1")) {
							b = true;
							str += '1';
						} else str += '0';
						
						col.getPixelControllerShufflerSelect().setShufflerSelect(i, b);
					}
					LOG.log(Level.INFO, "Shuffler select: "+str);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_ROTOZOOM:
				try {					
					int val = Integer.parseInt(msg[1]);
					RotoZoom r = (RotoZoom)col.getPixelControllerEffect().getEffect(EffectName.ROTOZOOM);
					r.setAngle(val);					
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case SAVE_PRESET:
				try {
					int idxs = col.getSelectedPreset();
					List<String> present = col.getCurrentStatus();
					col.getPresets().get(idxs).setPresent(present);
					col.savePresets();					
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case LOAD_PRESET:
				try {
					loadPreset(col.getSelectedPreset());
					col.notifyGuiUpdate();					
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_PRESET:
				try {
					int a = Integer.parseInt(msg[1]);
					if (a<Collector.NR_OF_PRESET_SLOTS) {
					    col.setSelectedPreset(a);					    
					}					
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_THRESHOLD_VALUE:
				try {
					int a = Integer.parseInt(msg[1]);
					if (a>255) {
						a=255;
					}
					if (a<0) {
						a=0;
					}
					col.getPixelControllerEffect().setThresholdValue(a);
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case BLINKEN:
				try {
					String fileToLoad = msg[1];
					col.getPixelControllerGenerator().setFileBlinken(fileToLoad);											
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case IMAGE:
				try {
					String fileToLoad = msg[1];
				    col.getPixelControllerGenerator().setFileImageSimple(fileToLoad);   
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
			
			case COLOR_SCROLL_OPT:
				try {
					int dir = Integer.parseInt(msg[1]);
					col.getPixelControllerGenerator().setColorScrollingDirection(dir);
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;


            case TEXTDEF:
				try {
					int lut = Integer.parseInt(msg[1]);
					col.getPixelControllerEffect().setTextureDeformationLut(lut);
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
            case ZOOMOPT:
				try {
					int zoomMode = Integer.parseInt(msg[1]);
					col.getPixelControllerEffect().setZoomOption(zoomMode);
				} catch (Exception e) {
					LOG.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case TEXTWR:
				try {
					String message = msg[1];
					col.getPixelControllerGenerator().setText(message);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case TEXTWR_OPTION:
				try {
					int scollerNr = Integer.parseInt(msg[1]);
					col.getPixelControllerGenerator().setTextOption(scollerNr);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;
				
			case RANDOM:	//enable or disable random mode
				try {
					String onOrOff = msg[1];
					if (onOrOff.equalsIgnoreCase("ON") || onOrOff.equalsIgnoreCase("1")) {
						col.setRandomPresetMode(false);
						col.setRandomMode(true);
						LOG.log(Level.INFO, "Random Mode enabled");
					}
					if (onOrOff.equalsIgnoreCase("OFF") || onOrOff.equalsIgnoreCase("0")) {
						col.setRandomPresetMode(false);
						col.setRandomMode(false);
						LOG.log(Level.INFO, "Random Mode disabled");
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case RANDOM_PRESET_MODE:
				try {
					String onOrOff = msg[1];
					if (onOrOff.equalsIgnoreCase("ON") || onOrOff.equalsIgnoreCase("1")) {
						col.setRandomMode(false);
						col.setRandomPresetMode(true);
						LOG.log(Level.INFO, "Random Preset Mode enabled");
					}
					if (onOrOff.equalsIgnoreCase("OFF") || onOrOff.equalsIgnoreCase("0")) {
						col.setRandomMode(false);
						col.setRandomPresetMode(false);
						LOG.log(Level.INFO, "Random Preset Mode disabled");
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}				
				break;
				
			case RANDOMIZE:	//one shot randomizer
				try {
					//save current visual buffer
					TransitionManager transition = new TransitionManager(col);
					Shuffler.manualShuffleStuff();
					transition.startCrossfader();
					col.notifyGuiUpdate();
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case PRESET_RANDOM:	//one shot randomizer, use a pre-stored present
				try {
					int currentPreset = Shuffler.getRandomPreset();					
					loadPreset(currentPreset);
					col.setSelectedPreset(currentPreset);
					col.notifyGuiUpdate();
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CURRENT_VISUAL:
				//change the selected visual, need to update
				//some of the gui elements 
				try {
					int a = Integer.parseInt(msg[1]);
					col.setCurrentVisual(a);
					col.notifyGuiUpdate();
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CURRENT_OUTPUT:
				//change the selected output, need to update
				//some of the gui elements 
				try {
					int a = Integer.parseInt(msg[1]);
					col.setCurrentOutput(a);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_BRIGHTNESS:
				try {
					int a = Integer.parseInt(msg[1]);
					if (a<0 || a>100) {
						LOG.log(Level.WARNING, IGNORE_COMMAND+", invalid brightness value: "+a);
						break;
					} else {
						float f = a/100f;
						col.getPixelControllerGenerator().setBrightness(f);
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;
				
			case GENERATOR_SPEED:
				try {
					int fpsAdjustment = Integer.parseInt(msg[1]);
					if (fpsAdjustment<0 || fpsAdjustment>200) {
						LOG.log(Level.WARNING, IGNORE_COMMAND+", invalid fps adjustment value: "+fpsAdjustment);
						break;
					} else {
						float f = fpsAdjustment/100f;
						col.getPixelControllerGenerator().setFpsAdjustment(f);
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				break;
				
			//create a screenshot of all current buffers
			case SCREENSHOT:
				col.saveScreenshot();
				LOG.log(Level.INFO, "Saved some screenshots");
				break;
				
			//change current colorset
			case CURRENT_COLORSET:
				int nr = col.getCurrentVisual();
				try {
					//old method, reference colorset by index
					int newColorSetIndex = Integer.parseInt(msg[1]);				                
	                col.getVisual(nr).setColorSet(newColorSetIndex);
	                break;
				} catch (Exception e) {
					//ignore
				}
				
				try {
					//now try to reference colorset by name
					col.getVisual(nr).setColorSet(msg[1]);
				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}

				break;
								
			//pause output, needed to create screenshots or take an image of the output
			case FREEZE:
				col.togglePauseMode();
				break;
			
			//show/hide internal visuals to save cpu power
			case TOGGLE_INTERNAL_VISUAL:
				col.toggleInternalVisual();
				break;
				
			case OSC_GENERATOR1:
				col.getPixelControllerGenerator().getOscListener1().updateBuffer(blob);
				break;
				
			case OSC_GENERATOR2:
				col.getPixelControllerGenerator().getOscListener2().updateBuffer(blob);
				break;

			case BEAT_WORKMODE:
				try {
					int workmodeId = Integer.parseInt(msg[1]);
					for (BeatToAnimation bta: BeatToAnimation.values()) {
						if (bta.getId() == workmodeId) {
							col.getPixelControllerGenerator().setBta(bta);
							LOG.log(Level.INFO, "Select beat workmode "+bta);
						}
					}

				} catch (Exception e) {
					LOG.log(Level.WARNING, IGNORE_COMMAND, e);
				}
				
				break;
				
			//unkown message
			default:
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<msg.length;i++) {
					sb.append(msg[i]);
					sb.append("; ");
				}
				LOG.log(Level.INFO,	"Ignored command <{0}>", sb);
				break;
			}
		} catch (IllegalArgumentException e) {
			LOG.log(Level.INFO,	"Unknown attribute ignored <{0}>", new Object[] { msg[0] });			
		}		
	}
	
	
	/**
	 * 
	 * @param nr
	 */
	private static void loadPreset(int nr) {
		Collector col = Collector.getInstance();
		
		//save current selections
		int currentVisual = col.getCurrentVisual();
		int currentOutput = col.getCurrentOutput();
							
		List<String> present = col.getPresets().get(nr).getPresent();					
		if (present!=null) {	
			//save current visual buffer
			TransitionManager transition = new TransitionManager(col);
			
			//load preset
			col.setCurrentStatus(present);
			
			//Restore current Selection 
			col.setCurrentVisual(currentVisual);
			col.setCurrentOutput(currentOutput);
			
			//start preset fader here, hardcoded to Crossfading
			transition.startCrossfader();
		}		
	}
	
}
