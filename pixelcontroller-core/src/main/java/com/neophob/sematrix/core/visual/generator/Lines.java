package com.neophob.sematrix.core.visual.generator;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lines extends Generator {

    class Line {
        public int startX;
        public int startY;
        public int goalX;
        public int goalY;
        public int[] buffer;
        public int bufferXSize;
        public int bufferYSize;
        public int color;
        private int dir; //-1 or 1

        public Line(final int startX, final int StartY, final int goalX, final int goalY, int[] buffer,
                    final int bufferXSize, final int bufferYSize, final int color) {

            this.startX = startX;
            startY = StartY;
            this.goalX = goalX;
            this.goalY = goalY;
            this.buffer = buffer;
            this.bufferXSize = bufferXSize;
            this.bufferYSize = bufferYSize;
            this.color = color;
            this.dir = 1;
        }


        public void draw() {
            line(startX, startY, goalX, goalY,buffer, bufferXSize, bufferYSize, color);
        }

        public void moveGoalX(final int length) {
            goalX += length * dir;
            if(goalX >= bufferXSize - 1) {
                goalX = bufferXSize - 1;
                dir *= -1;
            }
            if(goalX <= 0) {
                goalX = 0;
                dir *= -1;
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

    List<Line> lines = new ArrayList<Line>();
    public Lines(MatrixData matrix, GeneratorName name, ResizeName resizeOption) {
        super(matrix, name, resizeOption);
        lines.add(new Line(getInternalBufferXSize()/2, 0, getInternalBufferXSize()/2, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                           getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 -1, 0, getInternalBufferXSize()/2 - 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 -2, 0, getInternalBufferXSize()/2 - 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 + 1, 0, getInternalBufferXSize()/2 + 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 +2, 0, getInternalBufferXSize()/2 - 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 + 3, 0, getInternalBufferXSize()/2 + 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));
        lines.add(new Line(getInternalBufferXSize()/2 -3, 0, getInternalBufferXSize()/2 - 1, getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),127));


        lines.add(new Line(getInternalBufferXSize() / 2, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 - 1, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 -1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 + 1, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 + 1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 - 2, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 -1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 + 2, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 + 1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 - 3, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 -1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
        lines.add(new Line(getInternalBufferXSize() / 2 + 3, getInternalBufferYSize() - 1,getInternalBufferXSize() / 2 + 1,
                getInternalBufferYSize()/2, internalBuffer, getInternalBufferXSize(),
                getInternalBufferYSize(),80));
    }

    @Override
    public void update(int amount) {

        Arrays.fill(this.internalBuffer, 0);
        for(Line l : lines) {
            l.draw();
            l.moveGoalX(1);
        }

    }
}