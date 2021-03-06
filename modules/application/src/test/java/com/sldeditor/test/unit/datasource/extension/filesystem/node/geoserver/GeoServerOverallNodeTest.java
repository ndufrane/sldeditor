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
package com.sldeditor.test.unit.datasource.extension.filesystem.node.geoserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.sldeditor.common.filesystem.FileSystemInterface;
import com.sldeditor.datasource.extension.filesystem.dataflavour.BuiltInDataFlavour;
import com.sldeditor.datasource.extension.filesystem.node.geoserver.GeoServerOverallNode;

/**
 * Unit test for GeoServerOverallNode class.
 * 
 * <p>{@link com.sldeditor.datasource.extension.filesystem.node.geoserver.GeoServerOverallNode}
 * @author Robert Ward (SCISYS)
 *
 */
public class GeoServerOverallNodeTest {

    /**
     * Test method for {@link com.sldeditor.datasource.extension.filesystem.node.geoserver.GeoServerOverallNode#GeoServerOverallNode(com.sldeditor.common.filesystem.FileSystemInterface)}.
     */
    @Test
    public void testGeoServerLayerNode() {
        FileSystemInterface fileHandler = new DummyFileSystemInput();

        GeoServerOverallNode node = new GeoServerOverallNode(fileHandler);
        
        assertEquals(fileHandler, node.getHandler());
        assertEquals(BuiltInDataFlavour.GEOSERVER_OVERALL_DATAITEM_FLAVOUR, node.getDataFlavour());

        assertNull(node.getDestinationText());
    }
}
