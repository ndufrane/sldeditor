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
package com.sldeditor.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sldeditor.common.Controller;
import com.sldeditor.common.SLDDataInterface;
import com.sldeditor.common.SLDEditorInterface;
import com.sldeditor.common.console.ConsoleManager;
import com.sldeditor.common.localisation.Localisation;
import com.sldeditor.common.undo.UndoManager;
import com.sldeditor.common.undo.UndoStateInterface;
import com.sldeditor.datasource.SLDEditorDataUpdateInterface;
import com.sldeditor.datasource.SLDEditorFile;
import com.sldeditor.extension.ExtensionInterface;
import com.sldeditor.filter.v2.envvar.EnvironmentVariableManager;
import com.sldeditor.ui.about.AboutDialog;
import com.sldeditor.ui.preferences.PrefManagerUI;

/**
 * Class that creates and handles the application menus.
 * 
 * @author Robert Ward (SCISYS)
 */
public class SLDEditorMenus implements SLDEditorDataUpdateInterface, UndoStateInterface
{

    /** The Constant APPLICATION_FILE_EXTENSION. */
    private static final String APPLICATION_FILE_EXTENSION = "sldeditor";

    /** The Constant SLD_FILE_EXTENSION. */
    private static final String SLD_FILE_EXTENSION = "sld";

    /** The instance. */
    private static SLDEditorMenus instance = null;

    /** The menu save sld editor file. */
    private JMenuItem menuSaveSLDEditorFile;

    /** The application. */
    private SLDEditorInterface application = null;

    /** The undo menu item. */
    private JMenuItem menuItemUndo = null;

    /** The redo menu item. */
    private JMenuItem menuItemRedo = null;

    /** The menu save sld file. */
    private JMenuItem menuSaveSLDFile;

    /**
     * Instantiates a new SLD editor menus object.
     *
     * @param application the application
     */
    public SLDEditorMenus(SLDEditorInterface application)
    {
        this.application = application;

        SLDEditorFile.getInstance().addSLDEditorFileUpdateListener(this);
        UndoManager.getInstance().addListener(this);
    }

    /**
     * Creates the menus.
     *
     * @param application the application
     * @param extensionList the extension list
     */
    public static void createMenus(SLDEditorInterface application, List<ExtensionInterface> extensionList)
    {
        if(instance == null)
        {
            instance = new SLDEditorMenus(application);
        }

        instance.populate(extensionList);
    }

    /**
     * Populate menu structures.
     *
     * @param extensionList the extension list
     */
    private void populate(List<ExtensionInterface> extensionList)
    {
        JPanel appPanel = application.getAppPanel();

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        application.getApplicationFrame().setJMenuBar(menuBar);

        //
        // File menu
        //
        createFileMenu(appPanel, menuBar);

        //
        // Edit menu
        //
        createEditMenu(menuBar);

        //
        // SLD menu
        //
        createSLDMenu(appPanel, menuBar);

        //
        // Tools menu
        //
        createToolsMenu(extensionList, menuBar);
    }

    /**
     * Creates the edit menu.
     *
     * @param menuBar the menu bar
     */
    private void createEditMenu(JMenuBar menuBar)
    {
        JMenu mnEdit = new JMenu(Localisation.getString(SLDEditorMenus.class, "edit.menu"));
        menuBar.add(mnEdit);

        menuItemUndo = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "edit.menu.undo"), KeyEvent.VK_U);
        menuItemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        menuItemUndo.setEnabled(false);

        menuItemUndo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                UndoManager.getInstance().undo();
            }
        });

        mnEdit.add(menuItemUndo);

        menuItemRedo = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "edit.menu.redo"), KeyEvent.VK_R);
        menuItemRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        menuItemRedo.setEnabled(false);

        menuItemRedo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                UndoManager.getInstance().redo();
            }
        });
        mnEdit.add(menuItemRedo);
    }

    /**
     * Creates the tools menu.
     *
     * @param extensionList the extension list
     * @param menuBar the menu bar
     */
    private void createToolsMenu(List<ExtensionInterface> extensionList,
            JMenuBar menuBar)
    {
        JMenu mnTools = new JMenu(Localisation.getString(SLDEditorMenus.class, "tools.menu"));
        menuBar.add(mnTools);

        JMenuItem mntmEnvVar = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "tools.menu.envvar"));
        mntmEnvVar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EnvironmentVariableManager.getInstance().showDialog();
            }
        });
        mnTools.add(mntmEnvVar);

        JMenuItem mntmOptions = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "tools.menu.options"));
        mntmOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PrefManagerUI.showPrefPanel();
            }
        });

        // Create any extension specific menu items
        for(ExtensionInterface extension : extensionList)
        {
            extension.createMenus(mnTools);
        }

        mnTools.add(mntmOptions);

        JMenuItem mntmAboutDlg = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "tools.menu.about"));
        mntmAboutDlg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutDialog ad = new AboutDialog();
                ad.setVisible(true);
            }
        });
        mnTools.add(mntmAboutDlg);
    }

    /**
     * Creates the sld menu.
     *
     * @param appPanel the app panel
     * @param menuBar the menu bar
     */
    private void createSLDMenu(JPanel appPanel, JMenuBar menuBar)
    {
        JMenu mnSld = new JMenu(Localisation.getString(SLDEditorMenus.class, "sld.menu"));
        menuBar.add(mnSld);

        JMenuItem mntmOpenSLD = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.open"));
        mntmOpenSLD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(Localisation.getString(SLDEditorMenus.class, "sld.files.filter") + " (*." + SLD_FILE_EXTENSION + ")", SLD_FILE_EXTENSION);
                fileChooser.setFileFilter(filter);

                try
                {
                    SLDDataInterface sldData = SLDEditorFile.getInstance().getSLDData();
                    if(sldData != null)
                    {
                        File sldFile = sldData.getSLDFile();
                        File f = new File(sldFile.getCanonicalPath());
                        if(f.exists())
                        {
                            fileChooser.setSelectedFile(f);
                        }
                    }
                } catch (IOException e1) {
                    ConsoleManager.getInstance().exception(this,  e1);
                }

                try {

                    int result = fileChooser.showOpenDialog(Controller.getInstance().getFrame());
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        application.openFile(selectedFile.toURI().toURL());
                    }           
                } catch (IOException e1) {
                    ConsoleManager.getInstance().exception(this,  e1);
                }
            }
        });
        mnSld.add(mntmOpenSLD);

        menuSaveSLDFile = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.save"));
        menuSaveSLDFile.setEnabled(false);

        menuSaveSLDFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                URL url = null;
                try {
                    url = SLDEditorFile.getInstance().getSLDData().getSLDFile().toURI().toURL();
                } catch (MalformedURLException e1) {
                    ConsoleManager.getInstance().exception(SLDEditorMenus.class, e1);
                }

                if(url != null)
                {
                    application.saveFile(url);
                }
            }
        });
        mnSld.add(menuSaveSLDFile);

        JMenuItem mntmSaveAsSLD = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.saveas"));
        mntmSaveAsSLD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save SLD File");

                FileNameExtensionFilter filter = new FileNameExtensionFilter(Localisation.getString(SLDEditorMenus.class, "sld.files.filter") + " (*." + SLD_FILE_EXTENSION + ")", SLD_FILE_EXTENSION);
                fileChooser.setFileFilter(filter);
                File sldFile = SLDEditorFile.getInstance().getSLDData().getSLDFile();
                if(sldFile != null)
                {
                    if(sldFile.getParentFile() != null)
                    {
                        fileChooser.setCurrentDirectory(sldFile.getParentFile());
                    }
                    fileChooser.setSelectedFile(sldFile);
                }
                int userSelection = fileChooser.showSaveDialog(appPanel);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    if (!fileChooser.getFileFilter().accept(fileToSave)) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + "." + SLD_FILE_EXTENSION);
                    }

                    try
                    {
                        SLDEditorFile.getInstance().setSldFile(fileToSave);
                        application.saveFile(fileToSave.toURI().toURL());
                    }
                    catch (MalformedURLException e1)
                    {
                        ConsoleManager.getInstance().exception(SLDEditorMenus.class, e1);
                    }
                }
            }
        });
        mnSld.add(mntmSaveAsSLD);
    }

    /**
     * Creates the file menu.
     *
     * @param appPanel the app panel
     * @param menuBar the menu bar
     */
    private void createFileMenu(JPanel appPanel, JMenuBar menuBar)
    {
        JMenu fileMenu = new JMenu(Localisation.getString(SLDEditorMenus.class, "file.menu"));
        menuBar.add(fileMenu);

        JMenuItem menuOpenSLDEditorFile = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.open"),
                KeyEvent.VK_O);

        menuOpenSLDEditorFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(Localisation.getString(SLDEditorMenus.class, "file.files.filter") + " (*." + APPLICATION_FILE_EXTENSION + ")", APPLICATION_FILE_EXTENSION);
                fileChooser.setFileFilter(filter);

                File f = SLDEditorFile.getInstance().getSldEditorFile();
                if((f != null) && f.exists())
                {
                    fileChooser.setSelectedFile(f);
                }

                int result = fileChooser.showOpenDialog(appPanel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try
                    {
                        application.openFile(selectedFile.toURI().toURL());
                    }
                    catch (MalformedURLException e1)
                    {
                        ConsoleManager.getInstance().exception(SLDEditorMenus.class, e1);
                    }
                    SLDEditorFile.getInstance().fileOpenedSaved();
                }
            }
        });
        menuSaveSLDEditorFile = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.save"), KeyEvent.VK_S);
        menuSaveSLDEditorFile.setEnabled(false);
        menuSaveSLDEditorFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File sldEditorFile = SLDEditorFile.getInstance().getSldEditorFile();
                try
                {
                    application.saveFile(sldEditorFile.toURI().toURL());
                }
                catch (MalformedURLException e1)
                {
                    ConsoleManager.getInstance().exception(SLDEditorMenus.class, e1);
                }
                SLDEditorFile.getInstance().fileOpenedSaved();
            }
        });

        JMenuItem menuSaveAsSLDEditorFile = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "common.saveas"),
                KeyEvent.VK_A);
        menuSaveAsSLDEditorFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser() {

                    /** The Constant serialVersionUID. */
                    private static final long serialVersionUID = 1L;

                    /* (non-Javadoc)
                     * @see javax.swing.JFileChooser#approveSelection()
                     */
                    @Override
                    public void approveSelection() {
                        File f = getSelectedFile();

                        if (f.exists()) {
                            int result = JOptionPane.showConfirmDialog(this, Localisation.getString(SLDEditorMenus.class, "file.sldeditor.save.config.title"), Localisation.getString(SLDEditorMenus.class, "file.sldeditor.save.config.message"), JOptionPane.YES_NO_CANCEL_OPTION);

                            switch (result) {
                            case JOptionPane.YES_OPTION:
                                super.approveSelection();
                                return;
                            case JOptionPane.NO_OPTION:
                                return;
                            case JOptionPane.CLOSED_OPTION:
                                cancelSelection();
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                cancelSelection();
                                return;
                            }
                        }

                        super.approveSelection();
                    }
                };

                fileChooser.setDialogTitle(Localisation.getString(SLDEditorMenus.class, "file.sldeditor.save.dialog"));

                fileChooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter(Localisation.getString(SLDEditorMenus.class, "file.files.filter") + " (*." + APPLICATION_FILE_EXTENSION + ")", APPLICATION_FILE_EXTENSION);

                fileChooser.setFileFilter(filter);

                int userSelection = fileChooser.showSaveDialog(appPanel);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    if (!fileChooser.getFileFilter().accept(fileToSave)) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + "." + APPLICATION_FILE_EXTENSION);
                    }
                    try
                    {
                        SLDEditorFile.getInstance().setSldEditorFile(fileToSave);
                        application.saveFile(fileToSave.toURI().toURL());
                    }
                    catch (MalformedURLException e1)
                    {
                        ConsoleManager.getInstance().exception(SLDEditorMenus.class, e1);
                    }
                    SLDEditorFile.getInstance().fileOpenedSaved();
                }
            }
        });

        JMenuItem mntmNew = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "file.menu.new"));
        mntmNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.chooseNewSLD();
            }
        });
        fileMenu.add(mntmNew);

        fileMenu.add(menuOpenSLDEditorFile);
        fileMenu.add(menuSaveSLDEditorFile);
        fileMenu.add(menuSaveAsSLDEditorFile);

        JMenuItem mntmExit = new JMenuItem(Localisation.getString(SLDEditorMenus.class, "file.menu.exit"));
        mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.exitApplication();
            }
        });
        fileMenu.add(mntmExit);
    }

    /**
     * SLD data updated.
     *
     * @param sldData the sld data
     * @param dataEditedFlag the data edited flag
     */
    @Override
    public void sldDataUpdated(SLDDataInterface sldData, boolean dataEditedFlag)
    {
        menuSaveSLDEditorFile.setEnabled((sldData.getSldEditorFile() != null) && dataEditedFlag);
        menuSaveSLDFile.setEnabled((sldData.getSLDFile() != null) && dataEditedFlag);

        if(application != null)
        {
            application.updateWindowTitle(dataEditedFlag);
        }
    }

    /**
     * Update undo redo state.
     *
     * @param undoAllowed the undo allowed
     * @param redoAllowed the redo allowed
     */
    /* (non-Javadoc)
     * @see com.sldeditor.undo.UndoStateInterface#updateUndoRedoState(boolean, boolean)
     */
    @Override
    public void updateUndoRedoState(boolean undoAllowed, boolean redoAllowed)
    {
        menuItemUndo.setEnabled(undoAllowed);
        menuItemRedo.setEnabled(redoAllowed);
    }
}
