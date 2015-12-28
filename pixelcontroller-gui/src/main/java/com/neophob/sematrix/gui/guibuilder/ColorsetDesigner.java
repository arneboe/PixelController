package com.neophob.sematrix.gui.guibuilder;

import com.neophob.sematrix.core.api.impl.PixelControllerServer;
import com.neophob.sematrix.core.visual.color.ColorSet;
import com.neophob.sematrix.core.visual.color.HsvColor;
import com.neophob.sematrix.core.visual.color.HsvColorSet;
import com.neophob.sematrix.core.visual.color.IColorSet;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PImage;

import javax.swing.*;
import java.awt.*;


public class ColorsetDesigner extends PApplet implements ControlListener {

    private static final String ADD_COLOR_TEXT = "ADD COLOR";
    private static final String SAVE_BUTTON_TEXT = "SAVE";
    private static final String REMVOE_COLOR_TEXT = "REMOVE COLOR";
    private static final String HSV_BUTTON_TEXT = "HSV";
    private static final String RGB_BUTTON_TEXT = "RGB";
    private static final String NAME_TEXT= "NAME";

    private ControlP5 cp5;
    private PImage colorSetImg = this.createImage(128, 10, PApplet.RGB);
    private IColorSet colorSet = null;
    private boolean debounce = false;
    private controlP5.Button addButton;
    private controlP5.Button removeButton;
    private controlP5.Button saveButton;
    private controlP5.Button rgbButton;
    private controlP5.Button hsvButton;
    private controlP5.Textfield nameField;
    private boolean hsv = false;

    public ColorsetDesigner()
    {
    }

    @Override
    public void setup() {
        super.setup();
        cp5 = new ControlP5(this);
        cp5.addListener(this);
        background(0, 0, 0);

        cp5.addTextlabel("Preview:", "PREVIEW:", 10, 10);
        nameField = cp5.addTextfield(NAME_TEXT, 200, 10, 100, 20);
        nameField.setText("NAME");
        hsvButton = cp5.addButton(HSV_BUTTON_TEXT, 0, 10, 60, 50, 20);
        rgbButton = cp5.addButton(RGB_BUTTON_TEXT, 0, 80, 60, 50, 20);
        addButton = cp5.addButton(ADD_COLOR_TEXT, 0, 10, 90, 70, 20);
        saveButton = cp5.addButton(SAVE_BUTTON_TEXT, 0, 10, 120, 70, 20);
        addButton.setVisible(false);
        removeButton = cp5.addButton(REMVOE_COLOR_TEXT, 0, 80, 90, 70, 20);
        removeButton.setVisible(false);
        saveButton.setVisible(false);
    }

    @Override
    public synchronized void draw() {
        background(0, 0, 0);

        //clear colorset image
        for(int y = 0; y < colorSetImg.height; ++y) {
            for(int x = 0; x < colorSetImg.width; ++x) {
                final int i = y * colorSetImg.width + x;
                colorSetImg.pixels[i] = x * 2;
            }
        }
        if(null != colorSet) {
            colorSetImg.pixels = colorSet.convertToColorSetImage(colorSetImg.pixels);
        }
        colorSetImg.updatePixels();
        this.image(colorSetImg, 50f, 10f);
        cp5.draw();
    }

    @Override
    public void controlEvent(ControlEvent controlEvent) {
        //FIXME HACK button events always happen twice because we are using the wrong event but i haven o idea how to use the button() event from inside java
        if(debounce) {
            debounce = false;
            return;
        }

        if(controlEvent.getName().equals(ADD_COLOR_TEXT)) {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", Color.RED);
            if(null != newColor) { //the user did not opt out
                //FIXME  ColorSet and HsvColorSet are stupid quickhacks...
                if(hsv) {
                    HsvColor col = HsvColor.fromRGB(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                    ((HsvColorSet)colorSet).appendColor(col);
                }
                else {
                    ((ColorSet)colorSet).appendColor(newColor.getRGB());
                }
                saveButton.setVisible(true);
            }
        }
        else if(controlEvent.getName().equals(REMVOE_COLOR_TEXT)) {
            if(hsv) {
                ((HsvColorSet)colorSet).removeLastColor();
            }
            else
            {
                ((ColorSet)colorSet).removeLastColor();
            }
            saveButton.setVisible(true);
        }
        else if(controlEvent.getName().equals(RGB_BUTTON_TEXT)) {
            removeButton.setVisible(true);
            addButton.setVisible(true);
            hsvButton.setVisible(false);
            colorSet = new ColorSet("NEW");
            hsv = false;
        }
        else if(controlEvent.getName().equals(HSV_BUTTON_TEXT)) {
            removeButton.setVisible(true);
            addButton.setVisible(true);
            rgbButton.setVisible(false);
            colorSet = new HsvColorSet("NEW");
            hsv = true;
        }
        else if(controlEvent.getName().equals(NAME_TEXT)) {
            if(null != colorSet) {
                colorSet.setName(controlEvent.getStringValue());
            }
        }
        else if(controlEvent.getName().equals(SAVE_BUTTON_TEXT)) {
            if(null != colorSet) {
                String text = nameField.getText().trim() + "=" + colorSet.toString();
                PixelControllerServer.appendColorPalette(text);
            }
        }

        debounce = true;
    }
}
