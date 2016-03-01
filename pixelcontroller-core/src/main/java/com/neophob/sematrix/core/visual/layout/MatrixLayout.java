package com.neophob.sematrix.core.visual.layout;

import com.neophob.sematrix.core.visual.OutputMapping;
import java.util.List;

/**
 * A layout with n rows and m columns.
 * Each element is a device.
 */
public class MatrixLayout extends Layout {


    public MatrixLayout(int row1Size, int row2Size) {
        super(LayoutName.MATRIX, row1Size, row2Size);
    }

    @Override
    public LayoutModel getDataForScreen(int screenNr, List<OutputMapping> ioMapping) {
        return null;
    }
}
