package com.neophob.sematrix.core.visual.generator;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatRangeOption;
import com.neophob.sematrix.core.visual.effect.Options.SelectionListOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lines2 extends Generator {

    class Line {
        public int y;
        public int width;
        public int[] buffer;
        public int bufferXSize;
        public int bufferYSize;
        public int color;

        public Line(final int y, final int bufferXSize, final int bufferYSize, final int color, int[] buffer) {
            this.y = y;
            this.buffer = buffer;
            this.bufferXSize = bufferXSize;
            this.bufferYSize = bufferYSize;
            this.color = color;
            this.width = 1;
        }

        public void move(int i)
        {
            y += i;
            y = y % bufferYSize;
        }


        public void draw()
        {
            for(int i = 0; i < width; ++i)
            {
                if(y + i < bufferYSize)
                    line(0, y + i, bufferXSize, y + i, buffer, bufferXSize, bufferYSize, color);
            }
        }

        //from http://www-lehre.inf.uos.de/~rzugic/compgra/jar/Blatt6_final/Blatt6/cg/grOb/Bresenham.java
        public void line(final int rx, final int ry, final int qx, final int qy, int[] buffer, final int bufferXSize,
                         final int bufferYSize, final int color) {
            int px = rx;
            int py = ry;
            int error, delta, threshold, dx, dy, inc_x, inc_y;
            dx = qx - px;
            dy = qy - py;

            if (dx > 0)
                inc_x = 1;
            else
                inc_x = -1;
            if (dy > 0)
                inc_y = 1;
            else
                inc_y = -1;

            if (Math.abs(dy) < Math.abs(dx)) {
                error = -Math.abs(dx);
                delta = 2 * Math.abs(dy);
                threshold = 2 * error;
                while (px != qx) {
                    buffer[py * bufferXSize + px] = color;
                    px += inc_x;
                    error = error + delta;
                    if (error > 0) {
                        py += inc_y;
                        error = error + threshold;
                    }
                }
            } else {

                error = -Math.abs(dy);
                delta = 2 * Math.abs(dx);
                threshold = 2 * error;
                while (py != qy) {
                    buffer[py * bufferXSize + px] = color;
                    py += inc_y;
                    error = error + delta;
                    if (error > 0) {
                        px += inc_x;
                        error = error + threshold;
                    }
                }
            }
        }
    }

    private FloatRangeOption widthOption = new FloatRangeOption("Width", 1, 20, 3);
    private FloatRangeOption amountOption = new FloatRangeOption("Amount", 1, 10, 1);
    private FloatRangeOption colorOption = new FloatRangeOption("Color", 1, 255, 1);

    List<Line> lines = new ArrayList<Line>();
    public Lines2(MatrixData matrix, GeneratorName name, ResizeName resizeOption) {
        super(matrix, name, resizeOption);
        options.add(widthOption);
        options.add(amountOption);
        options.add(colorOption);
        buildLines();

    }

    private void buildLines()
    {
        final int num = (int)amountOption.getValue();
        final int dist = getInternalBufferYSize() / num;
        for(int i = 0; i < num; ++i)
        {
            lines.add(new Line(i * dist, getInternalBufferXSize(), getInternalBufferYSize(), 127, internalBuffer));
        }
    }

    @Override
    public void update(int amount) {

        Arrays.fill(this.internalBuffer, 0);
        if(amountOption.changed())
        {
            lines.clear();
            buildLines();
        }
        for(Line l : lines) {
            l.width = (int)widthOption.getValue();
            l.color = (int)colorOption.getValue();
            l.move(amount);
            l.draw();
           // l.moveGoalX(1);
        }

    }
}