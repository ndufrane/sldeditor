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
package com.sldeditor.datasource.extension.filesystem.node.file;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.sldeditor.common.NodeInterface;
import com.sldeditor.common.console.ConsoleManager;
import com.sldeditor.common.filesystem.FileSystemInterface;
import com.sldeditor.common.utils.ExternalFilenames;
import com.sldeditor.common.watcher.FileSystemWatcher;
import com.sldeditor.common.watcher.FileWatcherUpdateInterface;
import com.sldeditor.datasource.extension.filesystem.dataflavour.BuiltInDataFlavour;
import com.sldeditor.datasource.extension.filesystem.dataflavour.DataFlavourManager;
import com.sldeditor.datasource.extension.filesystem.node.FileSystemNodeManager;

/**
 * File system tree node representing either a file or folder.
 * 
 * @author Robert Ward (SCISYS)
 */
public class FileTreeNode extends DefaultMutableTreeNode implements NodeInterface, FileWatcherUpdateInterface {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9156830446596206479L;

    /** The file handler map. */
    private static Map<String, FileHandlerInterface> fileHandlerMap = null;

    /** The tree model. */
    private static DefaultTreeModel treeModel = null;

    /** The file system input interface. */
    private static FileSystemInterface inputInterface = null;

    /**  File object for this node. */
    private boolean isRoot = false;

    private Path path;

    /** The name of the node. */
    private String name;

    /** The populated. */
    private boolean populated;

    /** The interim flag, true if we have been populated. */
    private boolean interim;

    /**  The is directory flag, true if it is!. */
    private boolean isDir = false;

    /** The file category. */
    private FileTreeNodeTypeEnum fileCategory = FileTreeNodeTypeEnum.SLD;

    /** The file watcher set flag. */
    private boolean fileWatcherSet = false;

    /**
     * Instantiates a new file tree node.
     *
     * @param parent the parent
     * @param name the name
     * @throws SecurityException the security exception
     * @throws FileNotFoundException the file not found exception
     */
    public FileTreeNode(File parent, String name)
            throws SecurityException, FileNotFoundException {
        this(parent.toPath(), name);
    }

    /**
     * Instantiates a new file tree node.
     *
     * @param parent the parent
     * @param name the name
     * @throws SecurityException the security exception
     * @throws FileNotFoundException the file not found exception
     */
    public FileTreeNode(Path parent, String name)
            throws SecurityException, FileNotFoundException {

        if((parent == null) || (name == null))
        {
            throw new FileNotFoundException();
        }

        this.name = name;

        // See if this node exists and whether it is a directory
        path = Paths.get(parent.toString(), name);
        isDir = Files.isDirectory(path); 

        setUserObject(this.name);

        if(isDir())
        {
            FileSystemWatcher.getInstance().addWatch(this, path);
            fileWatcherSet = true;
        }
    } 

    /**
     * Instantiates a new file tree node for a root folder.
     *
     * @param parent the parent
     * @throws SecurityException the security exception
     * @throws FileNotFoundException the file not found exception
     */
    public FileTreeNode(Path parent)
            throws SecurityException, FileNotFoundException {

        if(parent == null)
        {
            throw new FileNotFoundException();
        }

        this.name = parent.toString();

        path = parent;

        isDir = true; 

        isRoot = true;
        setUserObject(this.name);
    } 

    /**
     * Checks if is leaf.
     *
     * @return true, if is leaf
     */
    @Override
    public boolean isLeaf() {
        return !isDir; 
    } 

    /**
     * Gets the allows children.
     *
     * @return the allows children
     */
    @Override
    public boolean getAllowsChildren() {
        return isDir;
    } 

    /**
     * Populate directories.
     * For display purposes, we return our own name public String toString() { return name; } 
     * If we are a directory, scan our contents and populate
     * with children. In addition, populate those children
     * if the "descend" flag is true. We only descend once,
     * to avoid recursing the whole subtree.
     *
     * @param descend the descend
     * @return true, if successful
     */
    public boolean populateDirectories(boolean descend) {
        boolean addedNodes = false;

        if(!isRoot || (isRoot && descend))
        {
            // Do this only once 
            if (populated == false) {
                if (interim == true) { 
                    // We have had a quick look here before:
                    // remove the dummy node that we added last time
                    removeAllChildren();
                    interim = false; 
                } 

                // Get list of contents
                List<Path> names = new ArrayList<Path>();

                DirectoryStream<Path> stream = null;
                try {
                    stream = Files.newDirectoryStream( path );
                } catch (AccessDeniedException e) {
                    // Access was denied
                } catch (NotDirectoryException e) {
                    // Ignore
                } catch (IOException e) {
                    ConsoleManager.getInstance().exception(this, e);
                }

                if(stream != null)
                {
                    for (Path path : stream) {
                        names.add(path.getFileName());
                    }
                }

                // Process the directories
                for (Path filename : names) {
                    Path d = Paths.get(path.toString(), filename.toString());
                    try {
                        if (Files.isDirectory(d)) {
                            addFolder(descend, filename.toString());

                            addedNodes = true;

                            if (descend == false) {
                                // Only add one node if not descending 
                                break;
                            }
                        }
                        else if(Files.isRegularFile(d))
                        {
                            if(validFile(filename.toString()))
                            {
                                addFile(filename.toString());
                            }
                        } 
                    }
                    catch (Throwable t) {
                        // Ignore phantoms or access problems
                    } 
                }

                // If we were scanning to get all sub-directories,
                // or if we found no sub-directories, there is no
                // reason to look at this directory again, so
                // set populated to true. Otherwise, we set interim
                // so that we look again in the future if we need to
                if (descend == true || addedNodes == false) {
                    populated = true;

                    if(isDir() && !fileWatcherSet)
                    {
                        FileSystemWatcher.getInstance().addWatch(this, path);
                        fileWatcherSet = true;
                    }

                } else {
                    // Just set interim state
                    interim = true;
                }
            }
        }
        return addedNodes; 
    }

    /**
     * Adds the folder.
     *
     * @param descend the descend
     * @param name the name
     * @throws FileNotFoundException the file not found exception
     */
    private void addFolder(boolean descend, String name) throws FileNotFoundException {
        FileTreeNode node = new FileTreeNode(path, name);
        this.add(node);
        if (descend) {
            node.populateDirectories(false); 
        }
    }

    /**
     * Adds the file to folder.
     *
     * @param name the name of the file
     * @throws FileNotFoundException the file not found exception
     */
    private void addFile(String name) throws FileNotFoundException {

        FileTreeNode node = new FileTreeNode(path, name);
        this.add(node);

        FileHandlerInterface handler = fileHandlerMap.get(ExternalFilenames.getFileExtension(name));
        if(handler != null)
        {
            if(handler.populate(inputInterface, treeModel, node))
            {
                node.isDir = true;
            }
        }
    } 

    /**
     * Valid file.
     *
     * @param filename the filename
     * @return true, if successful
     */
    private boolean validFile(String filename) {
        String fileExtension = ExternalFilenames.getFileExtension(filename);

        if(fileHandlerMap != null)
        {
            return (fileHandlerMap.keySet().contains(fileExtension));
        }
        return false;
    }

    /**
     * Sets the file handler map.
     *
     * @param fileHandlerMap the file handler map
     */
    public static void setFileHandlerMap(Map<String, FileHandlerInterface> fileHandlerMap)
    {
        FileTreeNode.fileHandlerMap = fileHandlerMap;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile()
    {
        return path.toFile();
    }

    /**
     * Sets the tree model.
     *
     * @param model the new tree model
     */
    public static void setTreeModel(DefaultTreeModel model)
    {
        FileTreeNode.treeModel = model;
    }

    /**
     * Sets the input interface.
     *
     * @param inputInterface the new input interface
     */
    public static void setInputInterface(FileSystemInterface inputInterface)
    {
        FileTreeNode.inputInterface = inputInterface;
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    /* (non-Javadoc)
     * @see com.sldeditor.extension.input.NodeInterface#getHandler()
     */
    @Override
    public FileSystemInterface getHandler()
    {
        return inputInterface;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the data flavour.
     *
     * @return the data flavour
     */
    /* (non-Javadoc)
     * @see com.sldeditor.extension.input.NodeInterface#getDataFlavour()
     */
    @Override
    public DataFlavor getDataFlavour()
    {
        boolean isDirectory = isDir();

        DataFlavor flavour = isDirectory ? DataFlavourManager.FOLDER_DATAITEM_FLAVOR : BuiltInDataFlavour.FILE_DATAITEM_FLAVOR;

        return flavour;
    }

    /**
     * Refresh folder.
     */
    public void refreshFolder()
    {
        populated = false;
        interim = true;
        populateDirectories(false);
    }

    /**
     * Checks if is dir.
     *
     * @return true, if is dir
     */
    public boolean isDir()
    {
        return isDir;
    }

    /**
     * File added.
     *
     * @param f the folder/file added
     */
    @Override
    public void fileAdded(Path f) {

        String filename = f.getFileName().toString();
        if(Files.isRegularFile(f))
        {
            // File added
            if(validFile(filename))
            {
                try
                {
                    addFile(filename);

                    sort(this);
                    FileSystemNodeManager.refreshNode(this);
                }
                catch (Throwable t) {
                    // Ignore phantoms or access problems
                } 
            }
        }
        else
        {
            // Folder added
            boolean descend = false;
            try {
                addFolder(descend, filename);
            }
            catch (Throwable t) {
                // Ignore phantoms or access problems
            }
            sort(this);
            FileSystemNodeManager.refreshNode(this);
        }
    }

    /**
     * Sort child nodes alphabetically.
     *
     * @param node the node
     */
    private static void sort(DefaultMutableTreeNode node) {
        //sort alphabetically
        for(int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            String nt = child.getUserObject().toString();

            for(int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
                String np = prevNode.getUserObject().toString();

                if(nt.compareToIgnoreCase(np) > 0) {
                    node.insert(child, j);
                    node.insert(prevNode, i);
                }
            }

            if(child.getChildCount() > 0) {
                sort(child);
            }
        }

        // Put folders first - normal on Windows and some flavours of Linux but not on Mac OS X.
        for(int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            for(int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);

                if(!prevNode.isLeaf() && child.isLeaf()) {
                    node.insert(child, j);
                    node.insert(prevNode, i);
                }
            }
        }
    }

    /**
     * File modified.
     *
     * @param f the folder/file modified
     */
    @Override
    public void fileModified(Path f) {
        // Do nothing
    }

    /**
     * File deleted.
     *
     * @param f the folder/file deleted
     */
    @Override
    public void fileDeleted(Path f) {

        String filename = f.getFileName().toString();

        for(int childIndex = 0; childIndex < this.getChildCount(); childIndex ++)
        {
            FileTreeNode childNode = (FileTreeNode) this.getChildAt(childIndex);

            if(childNode.name.compareTo(filename) == 0)
            {
                this.remove(childIndex);
                break;
            }
        }

        FileSystemNodeManager.refreshNode(this);
    }

    /**
     * Gets the destination text.
     *
     * @return the destination text
     */
    @Override
    public String getDestinationText() {
        return getFile().getAbsolutePath();
    }

    /**
     * Gets the file category.
     *
     * @return the fileCategory
     */
    public FileTreeNodeTypeEnum getFileCategory() {
        return fileCategory;
    }

    /**
     * Sets the file category.
     *
     * @param fileCategory the fileCategory to set
     */
    public void setFileCategory(FileTreeNodeTypeEnum fileCategory) {
        this.fileCategory = fileCategory;
    }
}
