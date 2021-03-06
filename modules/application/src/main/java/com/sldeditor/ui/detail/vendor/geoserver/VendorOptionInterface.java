/*
 * SLD Editor - The Open Source Java SLD Editor
 *
 * Copyright (C) 2016, SCISYS UK Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sldeditor.ui.detail.vendor.geoserver;

import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.TextSymbolizer;

import com.sldeditor.common.vendoroption.VendorOptionVersion;
import com.sldeditor.ui.detail.BasePanel;
import com.sldeditor.ui.iface.UpdateSymbolInterface;

/**
 * The Interface VendorOptionInterface.
 * 
 * @author Robert Ward (SCISYS)
 */
public interface VendorOptionInterface {

    /**
     * Gets the vendor option.
     *
     * @return the vendor option
     */
    public VendorOptionVersion getVendorOption();

    /**
     * Populate for a text symbolizer.
     *
     * @param textSymbolizer the text symbolizer
     */
    public void populate(TextSymbolizer textSymbolizer);

    /**
     * Update symbol for a text symbolizer.
     *
     * @param textSymbolizer the text symbolizer
     */
    public void updateSymbol(TextSymbolizer textSymbolizer);

    /**
     * Gets the panel.
     *
     * @return the panel
     */
    public BasePanel getPanel();

    /**
     * Sets the parent panel.
     *
     * @param parent the new parent panel
     */
    public void setParentPanel(UpdateSymbolInterface parent);

    /**
     * Update symbol for a polygon symbolizer.
     *
     * @param polygonSymbolizer the polygon symbolizer
     */
    public void updateSymbol(PolygonSymbolizer polygonSymbolizer);

    /**
     * Populate for a polygon symbolizer.
     *
     * @param polygonSymbolizer the polygon symbolizer
     */
    public void populate(PolygonSymbolizer polygonSymbolizer);
}
