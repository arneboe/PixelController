package com.neophob.sematrix.core.visual.generator;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatRangeOption;
import com.neophob.sematrix.core.visual.effect.Options.SelectionListOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Lasers extends Generator {


    class Line {
        public int startX;
        public int startY;
        public int goalX;
        public int[] buffer;
        public int bufferXSize;
        public int bufferYSize;
        public int color;
        public int height;


        public Line(final int startX, final int StartY, final int goalX, final int height, int[] buffer,
                    final int bufferXSize, final int bufferYSize, final int color) {

            this.startX = startX;
            startY = StartY;
            this.goalX = goalX;
            this.buffer = buffer;
            this.bufferXSize = bufferXSize;
            this.bufferYSize = bufferYSize;
            this.color = color;
            this.height = height;
        }


        public void draw()
        {
            final int starty = Math.min(bufferYSize - 1, startY);
            final int endy = Math.min(bufferYSize - 1, startY + height);
            final int width = 3;
            final int startx = Math.max(0, startX - width);
            final int endx = Math.min(bufferXSize - 1, startX + width);
            for(int x = startx; x <= endx; ++x) {
                line(x, starty, x, endy, buffer, bufferXSize, bufferYSize, color);
            }
        }

        public void move(final int amount)
        {
            startY += amount;
            if(startY >= bufferYSize)
            {
                startY = 0;
                startX = Lasers.rand.nextInt(bufferXSize);
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

    private FloatRangeOption heightOption = new FloatRangeOption("Height", 1, 20, 5);
    private FloatRangeOption amountOption = new FloatRangeOption("Amount", 1, 30, 1);
    private FloatRangeOption colorOption = new FloatRangeOption("Color", 1, 255, 1);
    public static Random rand = new Random();

    List<Line> lines = new ArrayList<Line>();
    public Lasers(MatrixData matrix, GeneratorName name, ResizeName resizeOption) {
        super(matrix, name, resizeOption);
        options.add(amountOption);
        options.add(colorOption);
        options.add(heightOption);
        buildLines();
    }

    private void buildLines()
    {
        final int num = (int)amountOption.getValue();
        final int  height = (int)heightOption.getValue();
        for(int i = 0; i < num; ++i)
        {
            final int x = rand.nextInt(getInternalBufferXSize());
            final int y = rand.nextInt(getInternalBufferYSize());
            Line l = new Line(x, y, x, height, internalBuffer, getInternalBufferXSize(), getInternalBufferYSize(), 127);
            lines.add(l);
        }
    }

    @Override
    public void update(int amount)
    {
        Arrays.fill(this.internalBuffer, 0);
        if(amountOption.changed())
        {
            lines.clear();
            buildLines();
        }
        for(Line l : lines) {
            l.height = (int)heightOption.getValue();
            l.color = (int)colorOption.getValue();
            l.move(amount);
            l.draw();
        }
    }
}