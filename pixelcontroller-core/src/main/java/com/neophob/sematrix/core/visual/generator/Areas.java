package com.neophob.sematrix.core.visual.generator;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatValueOption;

import java.util.ArrayList;

public class Areas extends Generator {

    class Rect
    {
        public int topLeftX;
        public int topLeftY;
        public int size;

        public Rect(final int x, final int y, final int size)
        {
            this.topLeftX = x;
            this.topLeftY = y;
            this.size = size;
        }

        public void draw(int[] buffer, final int bufferX, final int color)
        {
            for(int y = topLeftY; y < topLeftY + size; ++y)
            {
                for(int x = topLeftX; x < topLeftX + size; ++x)
                {
                    final int i = y * bufferX + x;
                    if(i < buffer.length)
                        buffer[i] = color;
                }
            }
        }

    }

    private FloatValueOption numOpt = new FloatValueOption("Count", 4, 40, 10);
    private ArrayList<Rect> areas = new ArrayList<Rect>();

    public Areas(MatrixData matrix, GeneratorName name, ResizeName resizeOption) {
        super(matrix, name, resizeOption);
        options.add(numOpt);
        buildAreas();
    }

    private void buildAreas()
    {

    }

    @Override
    public void update(int amount)
    {
        int col = 0;
        for(Rect r : areas)
        {
            r.draw(internalBuffer, getInternalBufferXSize(), col);
            col+= 40;
        }
    }
}