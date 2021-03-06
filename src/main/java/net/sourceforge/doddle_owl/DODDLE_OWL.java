/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)

 * Project Website: http://doddle-owl.sourceforge.net/
 *
 * Copyright (C) 2004-2015 Yamaguchi Laboratory, Keio University. All rights reserved.
 *
 * This file is part of DODDLE-OWL.
 *
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.sourceforge.doddle_owl;

import gnu.getopt.*;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;

import net.sourceforge.doddle_owl.actions.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.ui.*;
import net.sourceforge.doddle_owl.utils.*;

import org.apache.log4j.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 *
 * @author Takeshi Morita
 *
 */
public class DODDLE_OWL extends JFrame {

    private OptionDialog optionDialog;
    private LogConsole logConsole;

    public static DODDLEPlugin doddlePlugin;

    public static Frame rootFrame;
    public static JRootPane rootPane;
    public static JDesktopPane desktop;
    public static JMenu projectMenu;
    public static JMenu recentProjectMenu;
    public static StatusBarPanel STATUS_BAR;
    public static Set<String> GENERAL_ONTOLOGY_NAMESPACE_SET;

    private NewProjectAction newProjectAction;
    private OpenProjectAction openProjectAction;
    private SaveProjectAction saveProjectAction;
    private SaveProjectAsAction saveProjectAsAction;
    private LoadDescriptionsAction loadDescriptionAction;
    private LoadConceptPreferentialTermAction loadConceptDisplayTermAction;
    private SaveConceptPreferentialTermAction saveConceptDisplayTermAction;
    private LoadOntologyAction loadOWLOntologyAction;
    private LoadOntologyAction loadFreeMindOntologyAction;
    private SaveOntologyAction saveOWLOntologyAction;
    private SaveOntologyAction saveFreeMindOntologyAction;
    private ShowLogConsoleAction showLogConsoleAction;
    private ShowOptionDialogAction showOptionDialogAction;
    private ShowVersionInfoAction showVersionInfoAction;

    private ShowDODDLEDicConverterAction showDODDLEDicConverterAction;

    @Override
    public Image getIconImage() {
        return super.getIconImage();
    }

    private LayoutDockingWindowAction xgaLayoutDockingWindowAction;
    private LayoutDockingWindowAction uxgaLayoutDockingWindowAction;

    public static final Property HASA_PROPERTY = ResourceFactory.createProperty(DODDLEConstants.BASE_URI + "partOf");

    public DODDLE_OWL() {
        rootPane = getRootPane();
        rootFrame = this;
        desktop = new JDesktopPane();
        optionDialog = new OptionDialog(this);
        setFileLogger();
        logConsole = new LogConsole(this, Translator.getTerm("LogConsoleDialog"), null);
        STATUS_BAR = new StatusBarPanel();
        GENERAL_ONTOLOGY_NAMESPACE_SET = new HashSet<String>();
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDR_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDRT_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.WN_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.JPN_WN_URI);

        Container contentPane = getContentPane();
        makeActions();
        makeMenuBar();
        contentPane.add(getToolBar(), BorderLayout.NORTH);
        contentPane.add(desktop, BorderLayout.CENTER);
        contentPane.add(STATUS_BAR, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setIconImage(Utils.getImageIcon("application.png").getImage());
        setTitle(Translator.getTerm("ApplicationName") + " - " + Translator.getTerm("VersionMenu") + ": "
                + DODDLEConstants.VERSION);
        File tempDir = new File(Utils.TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        setVisible(true);
    }

    public static DODDLEPlugin getDODDLEPlugin() {
        return doddlePlugin;
    }

    public OptionDialog getOptionDialog() {
        return optionDialog;
    }

    public void exit() {
        int messageType = JOptionPane.showConfirmDialog(rootPane, Translator.getDescription("ExitAction"),
                Translator.getTerm("ExitAction"), JOptionPane.YES_NO_OPTION);
        if (messageType == JOptionPane.YES_OPTION) {
            if (isExistingCurrentProject()) {
                getCurrentProject().getDocumentSelectionPanel().destroyProcesses();
                getCurrentProject().getOntologySelectionPanel().closeDataset();
            }
            if (doddlePlugin == null) {
                System.exit(0);
            } else {
                dispose();
            }
        }
    }

    public List<String> loadRecentProject() {
        BufferedReader reader = null;
        List<String> recentProjects = new ArrayList<String>();
        try {
            File recentProjectFile = new File(DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE);
            if (!recentProjectFile.exists()) { return recentProjects; }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(recentProjectFile), "UTF-8"));
            while (reader.ready()) {
                recentProjects.add(reader.readLine());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        return recentProjects;
    }

    public void saveRecentProject(List<String> recentProjects) {
        BufferedWriter writer = null;
        try {
            File recentProjectFile = new File(DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE);
            if (!recentProjectFile.exists()) { return; }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recentProjectFile), "UTF-8"));
            int cnt = 0;
            DODDLE_OWL.recentProjectMenu.removeAll();
            for (String project : recentProjects) {
                if (cnt == 10) {
                    break;
                }
                writer.write(project);
                writer.write("\n");
                JMenuItem item = new JMenuItem(project);
                item.addActionListener(new OpenRecentProjectAction(project, this));
                DODDLE_OWL.recentProjectMenu.add(item);
                cnt++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void finishNewProject(DODDLEProject project) {
        try {
            desktop.add(project);
            project.toFront();
            desktop.setSelectedFrame(project);
            project.setVisible(true);
            project.setMaximum(true); // setVisibleより前にしてしまうと，初期サイズ(800x600)
            // で最大化されてしまう
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeActions() {
        newProjectAction = new NewProjectAction(Translator.getTerm("NewProjectAction"));
        openProjectAction = new OpenProjectAction(Translator.getTerm("OpenProjectAction"), this);
        saveProjectAction = new SaveProjectAction(Translator.getTerm("SaveProjectAction"), this);
        saveProjectAsAction = new SaveProjectAsAction(Translator.getTerm("SaveAsProjectAction"), this);
        loadDescriptionAction = new LoadDescriptionsAction(Translator.getTerm("OpenDescriptionsAction"));
        loadConceptDisplayTermAction = new LoadConceptPreferentialTermAction(
                Translator.getTerm("OpenConceptPreferentialTermMapAction"));
        saveConceptDisplayTermAction = new SaveConceptPreferentialTermAction(
                Translator.getTerm("SaveConceptPreferentialTermMapAction"));
        saveOWLOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveOWLOntologyAction"),
                SaveOntologyAction.OWL_ONTOLOGY);
        saveFreeMindOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveFreeMindOntologyAction"),
                SaveOntologyAction.FREEMIND_ONTOLOGY);
        loadOWLOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenOWLOntologyAction"),
                LoadOntologyAction.OWL_ONTOLOGY);
        loadFreeMindOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenFreeMindOntologyAction"),
                LoadOntologyAction.FREEMIND_ONTOLOGY);
        showLogConsoleAction = new ShowLogConsoleAction(Translator.getTerm("ShowLogConsoleAction"), logConsole);
        showOptionDialogAction = new ShowOptionDialogAction(Translator.getTerm("ShowOptionDialogAction"), this);
        showVersionInfoAction = new ShowVersionInfoAction(this);
        showDODDLEDicConverterAction = new ShowDODDLEDicConverterAction("DODDLE Dic Converter");
        xgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.XGA_LAYOUT,
                Translator.getTerm("XGALayoutAction"));
        uxgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.UXGA_LAYOUT,
                Translator.getTerm("UXGALayoutAction"));
    }

    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Translator.getTerm("FileMenu"));
        fileMenu.add(newProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(openProjectAction);
        recentProjectMenu = new JMenu(Translator.getTerm("OpenRecentProjectsMenu"));
        List<String> recentProjects = loadRecentProject();
        saveRecentProject(recentProjects);
        fileMenu.add(recentProjectMenu);
        JMenu loadMenu = new JMenu(Translator.getTerm("OpenMenu"));
        loadMenu.add(new LoadInputTermSetAction(Translator.getTerm("OpenInputTermListAction")));
        loadMenu.add(new LoadTermInfoTableAction(Translator.getTerm("OpenInputTermTableAction")));
        loadMenu.add(loadDescriptionAction);
        loadMenu.add(new LoadTermEvalConceptSetAction(Translator.getTerm("OpenInputConceptSelectionResultAction")));
        loadMenu.add(new LoadTermConceptMapAction(Translator.getTerm("OpenInputTermConceptMapAction")));
        loadMenu.add(loadOWLOntologyAction);
        loadMenu.add(loadFreeMindOntologyAction);
        loadMenu.add(loadConceptDisplayTermAction);
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        JMenu saveMenu = new JMenu(Translator.getTerm("SaveMenu"));
        // saveMenu.add(new SaveMatchedWordList("辞書に載っていた入力語彙を保存"));
        saveMenu.add(new SaveInputTermSetAction(Translator.getTerm("SaveInputTermListAction")));
        saveMenu.add(new SaveTermInfoTableAction(Translator.getTerm("SaveInputTermTableAction")));
        saveMenu.add(new SaveTermEvalConceptSetAction(Translator.getTerm("SaveInputConceptSelectionResultAction")));
        saveMenu.add(new SaveTermConceptMapAction(Translator.getTerm("SaveInputTermConceptMapAction")));
        saveMenu.add(new SavePerfectlyMatchedTermAction(Translator.getTerm("SavePerfectlyMatchedTermListAction")));
        saveMenu.add(new SavePerfectlyMatchedTermWithCompoundWordAction(Translator
                .getTerm("SavePerfectlyMatchedTermCompoundWordMapAction")));
        saveMenu.add(saveOWLOntologyAction);
        saveMenu.add(saveFreeMindOntologyAction);
        saveMenu.add(saveConceptDisplayTermAction);
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction(Translator.getTerm("ExitAction"), this));
        menuBar.add(fileMenu);

        JMenu toolMenu = new JMenu(Translator.getTerm("ToolMenu"));
        toolMenu.add(new ShowAllTermAction(Translator.getTerm("ShowAllTermAction")));
        toolMenu.addSeparator();
        toolMenu.add(new AutomaticDisAmbiguationAction(Translator.getTerm("AutomaticInputConceptSelectionAction")));
        toolMenu.addSeparator();
        toolMenu.add(new ConstructNounTreeAction());
        toolMenu.add(new ConstructNounAndVerbTreeAction());
        toolMenu.addSeparator();
        toolMenu.add(showDODDLEDicConverterAction);
        toolMenu.addSeparator();
        toolMenu.add(showLogConsoleAction);
        toolMenu.addSeparator();
        toolMenu.add(xgaLayoutDockingWindowAction);
        toolMenu.add(uxgaLayoutDockingWindowAction);
        toolMenu.addSeparator();
        toolMenu.add(showOptionDialogAction);
        menuBar.add(toolMenu);

        projectMenu = new JMenu(Translator.getTerm("ProjectMenu"));
        menuBar.add(projectMenu);
        menuBar.add(getHelpMenu());
        setJMenuBar(menuBar);
    }

    private JMenu getHelpMenu() {
        JMenu menu = new JMenu(Translator.getTerm("HelpMenu"));
        menu.add(showVersionInfoAction);
        return menu;
    }

    private JToolBar getToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(newProjectAction).setToolTipText(newProjectAction.getTitle());
        toolBar.add(openProjectAction).setToolTipText(openProjectAction.getTitle());
        toolBar.add(saveProjectAction).setToolTipText(saveProjectAction.getTitle());
        toolBar.add(saveProjectAsAction).setToolTipText(saveProjectAsAction.getTitle());
        toolBar.addSeparator();
        toolBar.add(showDODDLEDicConverterAction).setToolTipText(showDODDLEDicConverterAction.getTitle());
        toolBar.addSeparator();
        toolBar.add(showLogConsoleAction).setToolTipText(showLogConsoleAction.getTitle());
        toolBar.add(showOptionDialogAction).setToolTipText(saveProjectAsAction.getTitle());
        toolBar.add(showVersionInfoAction).setToolTipText(showVersionInfoAction.getTitle());
        return toolBar;
    }

    public static DODDLEProject getCurrentProject() {
        if (desktop == null) { return null; }
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        if (currentProject == null) {
            currentProject = new DODDLEProject(Translator.getTerm("NewProjectAction"), 11);
        }
        return currentProject;
    }

    public static boolean isExistingCurrentProject() {
        if (desktop == null) { return false; }
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        return currentProject != null;
    }

    public static void addProjectMenuItem(JMenuItem item) {
        projectMenu.add(item);
    }

    public static void removeProjectMenuItem(JMenuItem item) {
        projectMenu.remove(item);
    }

    public void loadConceptDisplayTerm(DODDLEProject currentProject, File file) {
        loadConceptDisplayTermAction.loadIDPreferentialTerm(currentProject, file);
    }

    public void saveConceptDisplayTerm(DODDLEProject currentProject, File file) {
        saveConceptDisplayTermAction.saveIDPreferentialTerm(currentProject, file);
    }

    public void saveOntology(DODDLEProject currentProject, File file) {
        saveOWLOntologyAction.saveOWLOntology(currentProject, file);
    }

    public void loadOntology(DODDLEProject currentProject, File file) {
        loadOWLOntologyAction.loadOWLOntology(currentProject, file);
    }

    public static void setSelectedIndex(int index) {
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        currentProject.setSelectedIndex(index);
    }

    public void loadBaseURI(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            DODDLEConstants.BASE_URI = reader.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public static void setPath(Properties properties) {
        DODDLEConstants.EDR_HOME = properties.getProperty("EDR_HOME");
        DODDLEConstants.EDRT_HOME = properties.getProperty("EDRT_HOME");
        InputDocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
        InputDocumentSelectionPanel.Japanese_Morphological_Analyzer = properties
                .getProperty("Japanese_Morphological_Analyzer");
        InputDocumentSelectionPanel.Japanese_Dependency_Structure_Analyzer = properties
                .getProperty("Japanese_Dependency_Structure_Analyzer");
        DODDLEConstants.BASE_URI = properties.getProperty("BASE_URI");
        DODDLEConstants.BASE_PREFIX = properties.getProperty("BASE_PREFIX");
        DODDLEConstants.PROJECT_HOME = properties.getProperty("PROJECT_DIR");
        UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
        DODDLEConstants.LANG = properties.getProperty("LANG");
    }

    public static void setPath() {
        try {
            Preferences userPrefs = Preferences.userNodeForPackage(DODDLE_OWL.class);
            String[] keys = userPrefs.keys();
            if (0 < keys.length) {
                Properties properties = new Properties();
                for (int i = 0; i < keys.length; i++) {
                    properties.put(keys[i], userPrefs.get(keys[i], ""));
                }
                setPath(properties);
            }
        } catch (BackingStoreException bse) {
            bse.printStackTrace();
        }
    }

    public static void setProgressValue() {
        InputModule.INIT_PROGRESS_VALUE = 887253;
        // InputModule.INIT_PROGRESS_VALUE = 283517;
    }

    public static Logger getLogger() {
        return Logger.getLogger(DODDLE_OWL.class);
    }

    private static void setDefaultLoggerFormat() {
        for (Enumeration enumeration = Logger.getRootLogger().getAllAppenders(); enumeration.hasMoreElements();) {
            Appender appender = (Appender) enumeration.nextElement();
            if (appender.getName().equals("stdout")) {
                appender.setLayout(new PatternLayout("[%5p][%c{1}][%d{yyyy-MMM-dd HH:mm:ss}]: %m\n"));
            }
        }
    }

    public static void setFileLogger() {
        try {
            getLogger().setLevel(Level.INFO);
            setDefaultLoggerFormat();
            String file = DODDLEConstants.PROJECT_HOME + File.separator + "doddle_log.txt";
            if (new File(DODDLEConstants.PROJECT_HOME).exists()) {
                FileAppender appender = new FileAppender(new PatternLayout(
                        "[%5p][%c{1}][%d{yyyy-MMM-dd HH:mm:ss}]: %m\n"), file);
                appender.setName("LOG File");
                appender.setAppend(true);
                Logger.getRootLogger().addAppender(appender);
            }
            getLogger().log(Level.INFO, "Resource Dir: " + Utils.RESOURCE_DIR);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void initOptions(String[] args) {
        LongOpt[] longopts = new LongOpt[4];
        longopts[0] = new LongOpt("DEBUG", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[1] = new LongOpt("LANG", LongOpt.REQUIRED_ARGUMENT, null, 'l');
        longopts[2] = new LongOpt("Swoogle", LongOpt.NO_ARGUMENT, null, 's');
        Getopt g = new Getopt("DODDLE", args, "", longopts);
        g.setOpterr(false);

        setPath();
        setProgressValue();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'g':
                getLogger().setLevel(Level.DEBUG);
                DODDLEConstants.DEBUG = true;
                break;
            case 'l':
                DODDLEConstants.LANG = g.getOptarg();
                break;
            case 's':
                DODDLEConstants.IS_INTEGRATING_SWOOGLE = true;
                break;
            default:
                break;
            }
        }
        if (DODDLEConstants.LANG == null) {
            DODDLEConstants.LANG = "ja";
        }
    }

    public static String getExecPath() {
        if (doddlePlugin == null) { return "." + File.separator; }
        String jarPath = DODDLE_OWL.class.getClassLoader().getResource("").getFile();
        File file = new File(jarPath);
        String configPath = file.getAbsolutePath() + File.separator;
        return configPath;
    }

    public static void main(String[] args) {
        SplashWindow splashWindow = new SplashWindow(null);
        DODDLE_OWL.initOptions(args);
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
        try {
            ToolTipManager.sharedInstance().setEnabled(true);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new DODDLE_OWL();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.setVisible(false);
        }
    }
}