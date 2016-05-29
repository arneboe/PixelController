package com.neophob.sematrix.core.visual.layout;

import com.neophob.sematrix.core.visual.OutputMapping;
import java.util.List;

/**
 * A layout with n rows and m columns.
 * Each element is a device.
 */
public class MatrixLayout extends Layout {


    public MatrixLayout(final int rowCount, final int colCount) {
        super(LayoutName.MATRIX, rowCount, colCount);
    }

    @Override
    public LayoutModel getDataForScreen(int screenNr, List<OutputMapping> ioMapping) {
        final int visualId = ioMapping.get(screenNr).getVisualId();
        final int fxOnHowMayScreensX = this.howManyScreensShareThisFxOnTheXAxis(visualId, ioMapping);
        final int fxOnHowMayScreensY = this.howManyScreensShareThisFxOnTheYAxis(visualId, ioMapping);
        return new LayoutModel(fxOnHowMayScreensX, fxOnHowMayScreensY, this.getXOffsetForScreen(
                screenNr, fxOnHowMayScreensX, visualId, ioMapping), this.getYOffsetForScreen(
                screenNr, fxOnHowMayScreensY), visualId);
    }

    /**Return the index of the first row that contains this screen */
    private int getYOffsetForScreen(int screenNr, int fxOnHowMayScreens) {

        return 0;
    }

    private int getXOffsetForScreen(int screenNr, int fxOnHowMayScreens, int visualId,
                                    List<OutputMapping> ioMapping) {
        int ret = screenNr;
        while (ret >= colCount) {
            ret -= colCount;
        }

        if (fxOnHowMayScreens == 1 || ret == 0) {
            return 0;
        }

        // Get start X offset, example:
        //
        // O X X
        // O X X
        //
        // O = Visual 1
        // X = Visual 2
        //
        int xOfs = ret;
        for (int i = 0; i < ret; i++) {
            OutputMapping o1 = ioMapping.get(i);
            OutputMapping o2 = ioMapping.get(colCount + i);
            if ((o1.getVisualId() != visualId) && (o2.getVisualId() != visualId)) {
                if (xOfs > 0) {
                    xOfs--;
                }
            }
        }
        return xOfs;

    }

    private int howManyScreensShareThisFxOnTheYAxis(int visualId, List<OutputMapping> ioMapping) {
        int max = 0;
        int min = Integer.MAX_VALUE;
        OutputMapping o;

        for (int x = 0; x < colCount; x++) {
            for (int y = 0; y < rowCount; y++) {
                o = ioMapping.get(colCount * y + x);

                if (o.getVisualId() == visualId) {
                    if (y < min) {
                        min = y;
                    }
                    if (y + 1 > max) {
                        max = y + 1;
                    }
                }
            }
        }
        return max - min;


    }

    /** Returns the maximum number of screens that the visual spans in any row.*/
    private int howManyScreensShareThisFxOnTheXAxis(int visualId, List<OutputMapping> ioMapping) {

        int max = 0;
        int min = Integer.MAX_VALUE;
        OutputMapping o;

        for (int y = 0; y < rowCount; y++) {
            for (int x = 0; x < colCount; x++) {
                o = ioMapping.get(colCount * y + x);
                if (o.getVisualId() == visualId) {
                    if (x < min) {
                        min = x;
                    }
                    if (x + 1 > max) {
                        max = x + 1;
                    }
                }
            }
        }
        return max - min;
    }
}
