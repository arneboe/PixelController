package com.neophob.sematrix.core.visual.effect;

import com.neophob.sematrix.core.resize.Resize;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatValueOption;

import java.util.Random;

/**
 * Created by arne on 5/28/15.
 */
public class MovingWindow extends Effect {

    private int[] buffer;
    private FloatValueOption widthOption;
    private FloatValueOption heightOption;
    private FloatValueOption speedOption = new FloatValueOption("Speed",0.1f, 20, 1);

    private float x = 0;//current top left position of window
    private float y = 0;
    private float xSign = 1;
    private float ySign = 1;
    private final int bufferWidth;
    private final int bufferHeight;
    Random rand = new Random();
    public MovingWindow(MatrixData matrix) {
        super(matrix, EffectName.MOVING_WINDOW, Resize.ResizeName.PIXEL_RESIZE);
        buffer = new int[1];
        bufferWidth = matrix.getBufferXSize();
        bufferHeight = matrix.getBufferYSize();
        widthOption = new FloatValueOption("Width", 1, bufferWidth / 2, 10);
        heightOption = new FloatValueOption("Height", 1, bufferHeight / 2, 10);
        options.add(widthOption);
        options.add(heightOption);
        options.add(speedOption);

    }

    @Override
    public int[] getBuffer(int[] inputBuffer) {
        if(buffer.length != inputBuffer.length) {
            buffer = new int[inputBuffer.length];
        }

        //randomly change direction mid run
        if(rand.nextFloat() > 0.99f) {
            xSign *= -1;
        }
        if(rand.nextFloat() > 0.99f) {
            ySign *= -1;
        }
        final float speed = speedOption.getValue();
        //check if  movement is possible
        float dirX = rand.nextFloat() * speed * xSign;
        if(x + dirX < 0 || x + dirX + widthOption.getValue() >= bufferWidth) {
            xSign *= -1;
            dirX *= -1;
        }

        float dirY = rand.nextFloat() * speed * ySign;
        if(y + dirY < 0 || y + dirY + heightOption.getValue() >= bufferHeight) {
            ySign *= -1;
            dirY *= -1;
        }
        x += dirX;
        y += dirY;

        final int left = (int)x;
        final int right = (int)( x + widthOption.getValue());
        final int top = (int)y;
        final int bottom = (int)(y + heightOption.getValue());
        for(int yy = 0; yy < bufferHeight; ++yy) {
            for(int xx = 0; xx < bufferWidth; ++xx) {
                final int i = yy * bufferWidth + xx;
                if(xx >= left && xx < right &&
                   yy >= top && yy < bottom) {
                    buffer[i] = inputBuffer[i];
                }
                else {
                    buffer[i] = 0;
                }
            }
        }
        return buffer;
    }
}
