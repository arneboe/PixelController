/**
 * Copyright (C) 2011-2014 Michael Vogt <michu@neophob.com>
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
package com.neophob.sematrix.core.common;

import com.neophob.sematrix.core.common.MathHelpers;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MathHelpersTest {

    @Test
    public void linspaceAscTest() throws Exception {
        double[] spaced = MathHelpers.linspace(12, 15, 11);
        assertEquals(spaced.length, 11);
        assertEquals(spaced[0], 12.0, 0.00001);
        assertEquals(spaced[10], 15, 0.00001);
        for(int i = 1; i < spaced.length; ++i)
        {
            assertTrue(spaced[i-1] < spaced[i]);
        }
    }

    @Test
    public void linspaceAsc2Test() throws Exception {
        double[] spaced = MathHelpers.linspace(-12, 42, 9);
        assertEquals(spaced.length, 9);
        assertEquals(spaced[0], -12.0, 0.00001);
        assertEquals(spaced[8], 42, 0.00001);
        for(int i = 1; i < spaced.length; ++i)
        {
            assertTrue(spaced[i-1] < spaced[i]);
        }
    }

    @Test
    public void linspaceAllNegTest() throws Exception {
        double[] spaced = MathHelpers.linspace(-42, -22, 17);
        assertEquals(spaced.length, 17);
        assertEquals(spaced[0], -42.0, 0.00001);
        assertEquals(spaced[16], -22, 0.00001);
        for(int i = 1; i < spaced.length; ++i)
        {
            assertTrue(spaced[i-1] < spaced[i]);
        }
    }

}
