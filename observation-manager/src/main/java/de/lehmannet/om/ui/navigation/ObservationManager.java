/* ====================================================================
 * /navigation/ObservationManager.java
 *
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.navigation;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.lehmannet.om.IEyepiece;
import de.lehmannet.om.IFilter;
import de.lehmannet.om.IImager;
import de.lehmannet.om.ILens;
import de.lehmannet.om.IObservation;
import de.lehmannet.om.IObserver;
import de.lehmannet.om.ISchemaElement;
import de.lehmannet.om.IScope;
import de.lehmannet.om.ISession;
import de.lehmannet.om.ISite;
import de.lehmannet.om.ITarget;
import de.lehmannet.om.OALException;
import de.lehmannet.om.ui.dialog.AboutDialog;
import de.lehmannet.om.ui.dialog.AbstractDialog;
import de.lehmannet.om.ui.dialog.DidYouKnowDialog;
import de.lehmannet.om.ui.dialog.ExtensionInfoDialog;
import de.lehmannet.om.ui.dialog.EyepieceDialog;
import de.lehmannet.om.ui.dialog.FilterDialog;
import de.lehmannet.om.ui.dialog.IImagerDialog;
import de.lehmannet.om.ui.dialog.ITargetDialog;
import de.lehmannet.om.ui.dialog.LensDialog;
import de.lehmannet.om.ui.dialog.LogDialog;
import de.lehmannet.om.ui.dialog.OMDialog;
import de.lehmannet.om.ui.dialog.ObservationDialog;
import de.lehmannet.om.ui.dialog.ObserverDialog;
import de.lehmannet.om.ui.dialog.ProgressDialog;
import de.lehmannet.om.ui.dialog.ScopeDialog;
import de.lehmannet.om.ui.dialog.SessionDialog;
import de.lehmannet.om.ui.dialog.SiteDialog;
import de.lehmannet.om.ui.dialog.TableElementsDialog;
import de.lehmannet.om.ui.dialog.UnavailableEquipmentDialog;
import de.lehmannet.om.ui.extension.ExtensionLoader;
import de.lehmannet.om.ui.navigation.observation.utils.ArgumentName;
import de.lehmannet.om.ui.navigation.observation.utils.ArgumentsParser;
import de.lehmannet.om.ui.navigation.observation.utils.InstallDir;
import de.lehmannet.om.ui.navigation.observation.utils.SystemInfo;
import de.lehmannet.om.ui.preferences.PreferencesDialog;
import de.lehmannet.om.ui.project.ProjectCatalog;
import de.lehmannet.om.ui.project.ProjectLoader;
import de.lehmannet.om.ui.statistics.StatisticsDialog;
import de.lehmannet.om.ui.update.UpdateChecker;

import de.lehmannet.om.ui.update.UpdateInfoDialog;
import de.lehmannet.om.ui.util.Configuration;
import de.lehmannet.om.ui.util.ExtenableSchemaElementSelector;
import de.lehmannet.om.ui.util.SplashScreen;
import de.lehmannet.om.ui.util.Worker;
import de.lehmannet.om.ui.util.XMLFileLoader;
import de.lehmannet.om.util.FloatUtil;
import de.lehmannet.om.util.SchemaElementConstants;
import de.lehmannet.om.util.SchemaLoader;

public class ObservationManager extends JFrame implements ActionListener {

    private static final long serialVersionUID = -9092637724048070172L;

    // Config keys
    public static final String CONFIG_LASTDIR = "om.lastOpenedDir";
    public static final String CONFIG_LASTXML = "om.lastOpenedXML";
    public static final String CONFIG_OPENONSTARTUP = "om.lastOpenedXML.onStartup";
    public static final String CONFIG_CONTENTDEFAULTLANG = "om.content.language.default";
    public static final String CONFIG_MAINWINDOW_SIZE = "om.mainwindow.size";
    public static final String CONFIG_MAINWINDOW_POS = "om.mainwindow.position";
    public static final String CONFIG_MAINWINDOW_MAXIMIZED = "om.mainwindow.maximized";
    public static final String CONFIG_IMAGESDIR_RELATIVE = "om.imagesDir.relaitve";
    public static final String CONFIG_UILANGUAGE = "om.language";
    public static final String CONFIG_DEFAULT_OBSERVER = "om.default.observer";
    public static final String CONFIG_DEFAULT_CATALOG = "om.default.catalog";
    public static final String CONFIG_HELP_HINTS_STARTUP = "om.help.hints.showOnStartup";
    public static final String CONFIG_RETRIEVE_ENDDATE_FROM_SESSION = "om.retrieve.endDateFromSession";
    public static final String CONFIG_STATISTICS_USE_COOBSERVERS = "om.statistics.useCoObservers";
    public static final String CONFIG_XSL_TEMPLATE = "om.transform.xsl.template";
    public static final String CONFIG_MAINWINDOW_DIVIDER_VERTICAL = "om.mainwindow.divider.vertical";
    public static final String CONFIG_MAINWINDOW_DIVIDER_HORIZONTAL = "om.mainwindow.divider.horizontal";
    public static final String CONFIG_CONSTELLATION_USEI18N = "om.constellation.useI18N";
    public static final String CONFIG_UPDATECHECK_STARTUP = "om.update.checkForUpdates";
    public static final String CONFIG_NIGHTVISION_ENABLED = "om.nightvision.enable";
    // public static final String CONFIG_UPDATE_RESTART = "om.update.restart";

    // ResourceBundle will be set in constructor after default locale is defined
    public static PropertyResourceBundle bundle = null;

    private final Logger LOGGER = LoggerFactory.getLogger(ObservationManager.class);

    // Version
    public static final String VERSION = "1.421";

    
    // Working directory
    public static final String WORKING_DIR = ".observationManager";

    // ---------
    // Variables --------------------------------------------------------------
    // ---------
    private JSplitPane hSplitPane;
    private JSplitPane vSplitPane;

    private JMenuBar menuBar;
    private JMenuItem newFile;
    private JMenuItem openFile;
    // private JMenuItem openDir;
    private JMenuItem saveFile;
    private JMenuItem saveFileAs;
    private JMenuItem importXML;
    private JMenuItem exportHTML;
    private JCheckBoxMenuItem nightVision;
    private JMenuItem exit;

    private JMenuItem createObservation;
    private JMenuItem createObserver;
    private JMenuItem createSite;
    private JMenuItem createScope;
    private JMenuItem createEyepiece;
    private JMenuItem createImager;
    private JMenuItem createFilter;
    private JMenuItem createTarget;
    private JMenuItem createSession;
    private JMenuItem createLens;
    private JMenuItem equipmentAvailability;

    private JMenuItem showStatistics;
    private JMenuItem preferences;
    private JMenuItem didYouKnow;
    private JMenuItem logMenuEntry;
    private JMenuItem updateMenuEntry;

    private JMenuItem extensionInfo;
    private JMenuItem installExtension;

    private JMenuItem aboutInfo;

    private TableView table;
    private ItemView item;
    private TreeView tree;
    private ExtensionLoader extLoader;

    private Configuration configuration;
    private ProjectLoader projectLoader;

    private File logFile;

    private String configDir;

    private boolean changed = false; // Indicates if changed where made after
                                     // load.

    private Boolean nightVisionOnStartup;

    private Thread splash;

    private Thread waitForCatalogLoaderThread;

    private boolean debug = false; // Show debug information

    private InstallDir installDir;
    private ArgumentsParser argumentsParser;

    // this.installDir = new File(getArgValue(arg));

    private XMLFileLoader xmlCache;

    private ObservationManagerMenuFile menuFile;

    private File schemaPath;

    private Map<String,String> uiDataCache = new HashMap<>();

    // if ("de".equals(getArgValue(arg))) {
    // Locale.setDefault(Locale.GERMAN);
    // } else {
    // Locale.setDefault(Locale.ENGLISH);
    // }

    // this.nightVisionOnStartup = Boolean.parseBoolean(getArgValue(arg));
    // this.configDir = getArgValue(arg);
    // this.logDir = getArgValue(arg);
    // this.debug = true;

    public final InstallDir getInstallDir() {
        return this.installDir;
    }

    // -----------
    // Constructor ------------------------------------------------------------
    // -----------

    private ObservationManager(String[] args) {

      
        // Get install dir and parse arguments
        this.argumentsParser = new ArgumentsParser.Builder(args).build();

        final String installDirName = this.argumentsParser.getArgumentValue(ArgumentName.INSTALL_DIR);
        this.installDir = new InstallDir.Builder().withInstallDir(installDirName).build();
        this.configDir = this.argumentsParser.getArgumentValue(ArgumentName.CONFIGURATION);

        LOGGER.debug("Start: {}", new Date());
        LOGGER.debug(SystemInfo.printMemoryUsage());

        

        // Load configuration
        this.configuration = new Configuration(this.configDir);

        this.schemaPath = new File(this.installDir.getPathForFile("schema"));
        if (!this.schemaPath.exists()) {
            System.err.println("--- Comast schema path not found: " + this.schemaPath + "\n. Need to quit...");
        }
        // Initialize Caches and loaders
        this.xmlCache = new XMLFileLoader(this.schemaPath);

        this.menuFile = new ObservationManagerMenuFile(this.configuration, this.xmlCache);


        boolean nightVisionOnStartup = Boolean
                .parseBoolean(this.configuration.getConfig(ObservationManager.CONFIG_NIGHTVISION_ENABLED, "false"));
        if (this.nightVisionOnStartup != null) { // If set by command line, overrule config
            nightVisionOnStartup = this.nightVisionOnStartup;
        }

        // Load SplashScreen
        if (!nightVisionOnStartup) {
            this.splash = new Thread(new SplashScreen(this.installDir.getPath()));
            this.splash.start();
        }

        // After we checked arguments and configuration, we can load language
        // bundle (language might be set as argument or
        // configuration)
        this.loadLanguage();

        // Set title
        this.setTitle();

        // Set icon
        super.setIconImage(new ImageIcon(this.installDir.getPathForFile("om_logo.png")).getImage());

        LOGGER.info("--- Observation Manager {} starting up...", VERSION);

        // Write Java version into log
        LOGGER.info("--- Java:\t {} {} , " + System.getProperty("java.vendor"), System.getProperty("java.version"));
        LOGGER.info("--- OS:\t {} ({}) {}", System.getProperty("os.name"), System.getProperty("os.arch"),
                System.getProperty("os.version"));

        

        // this.loader = new SchemaUILoader(this);
        this.extLoader = new ExtensionLoader(this);
        // this.catLoader = new CatalogLoader(this.getInstallDir(), this);
       


        // Init menu and disable it during startup
        this.initMenuBar();
        this.enableMenus(false);

        // Set nightvision theme
        if (nightVisionOnStartup) {
            this.nightVision.setSelected(true);
            this.enableNightVisionTheme(true);
        }

        this.item = this.initItemView();
        this.table = this.initTableView();
        this.tree = this.initTreeView();

        this.initMain();

        // ****************************************************************
        // Only required for Auto. update, which is currently not supported
        //
        // Check on restart Update and perform required steps (of necessary)
        // this.performRestartUpdate();
        // ****************************************************************

        // Load XML File on startup (if desired)
        this.loadConfig();

        // Start loading of asynchronous project file(s)
        this.loadProjectFiles();

        // Check for updates
        if (Boolean
                .parseBoolean(this.configuration.getConfig(ObservationManager.CONFIG_UPDATECHECK_STARTUP, "false"))) {
            UpdateChecker updateChecker = this.checkForUpdates();
            
            if (updateChecker.isUpdateAvailable()) {
                        new UpdateInfoDialog(this, updateChecker);
            } else {
                    System.out.println("Checked for updates: No updates found.");
                
            }
        }

    // If we should show the hints on startup, do so now...
    if(Boolean.parseBoolean(this.configuration.getConfig(ObservationManager.CONFIG_HELP_HINTS_STARTUP,"true")))

    {
        this.showDidYouKnow();
    }

    // Add shortcut key listener
    this.addShortcuts();

    // We're up an running, so enable menus now
    this.enableMenus(true);

    if(this.debug)
    {
        System.out.println("Up and running: " + new Date());
        System.out.println(SystemInfo.printMemoryUsage());
    }

    }

    // --------------
    // ActionListener ---------------------------------------------------------
    // --------------

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() instanceof JMenuItem) {
            JMenuItem source = (JMenuItem) e.getSource();
            if (source.equals(this.exit)) {
                this.menuFile.exit(this, this.changed);
            } else if (source.equals(this.newFile)) {
                this.menuFile.newFile(this, this.changed);
            } else if (source.equals(this.openFile)) {
                this.menuFile.openFile(this, this.changed);
                /*
                 * } else if( source.equals(this.openDir) ) { this.openDir();
                 */
            } else if (source.equals(this.saveFile)) {
                this.menuFile.saveFile(this);
            } else if (source.equals(this.saveFileAs)) {
                this.menuFile.saveFileAs(this, this.changed);
            } else if (source.equals(this.importXML)) {
                this.importXML();
            } else if (source.equals(this.exportHTML)) {
                this.createHTML();
            } else if (source.equals(this.nightVision)) {
                if (this.nightVision.isSelected()) {
                    this.enableNightVisionTheme(true);
                } else {
                    this.enableNightVisionTheme(false);
                }
            } else if (source.equals(this.createObservation)) {
                this.createNewObservation();
            } else if (source.equals(this.createObserver)) {
                this.createNewObserver();
            } else if (source.equals(this.createSite)) {
                this.createNewSite();
            } else if (source.equals(this.createScope)) {
                this.createNewScope();
            } else if (source.equals(this.createEyepiece)) {
                this.createNewEyepiece();
            } else if (source.equals(this.createImager)) {
                this.createNewImager();
            } else if (source.equals(this.createFilter)) {
                this.createNewFilter();
            } else if (source.equals(this.createLens)) {
                this.createNewLens();
            } else if (source.equals(this.createTarget)) {
                this.createNewTarget();
            } else if (source.equals(this.createSession)) {
                this.createNewSession();
            } else if (source.equals(this.equipmentAvailability)) {
                UnavailableEquipmentDialog uqd = new UnavailableEquipmentDialog(this);
                this.setChanged(uqd.changedElements());
            } else if (source.equals(this.showStatistics)) {
                this.showStatistics();
            } else if (source.equals(this.preferences)) {
                this.showPreferencesDialog();
            } else if (source.equals(this.didYouKnow)) {
                this.showDidYouKnow();
            } else if (source.equals(this.logMenuEntry)) {
                this.showLogDialog();
            } else if (source.equals(this.updateMenuEntry)) {
                UpdateChecker checker = this.checkForUpdates();               
                if (checker.isUpdateAvailable()) {                               
                        new UpdateInfoDialog(this, checker);
                    
                } else { // Something went wrong
                    this.createInfo(ObservationManager.bundle.getString("updates.check.noAvailable"));
                    
                }
            } else if (source.equals(this.aboutInfo)) {
                this.showInfo();
            } else if (source.equals(this.extensionInfo)) {
                this.showExtensionInfo();
            } else if (source.equals(this.installExtension)) {
                this.installExtension(null);
            }
        }

    }

    // ------
    // JFrame -----------------------------------------------------------------
    // ------

    @Override
    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {

            if (this.menuFile.exit(this,this.changed)) {
                super.processWindowEvent(e);
                this.dispose();
            }

        }

    }

    // ----
    // Main -------------------------------------------------------------------
    // ----

    public static void main(String[] args) {

        new ObservationManager(args);

    }

    // --------------
    // Public Methods ---------------------------------------------------------
    // --------------

    private void showInfo() {

        new AboutDialog(this);

    }

    private void showStatistics() {

        if (this.getXmlCache().getObservations().length == 0) {
            this.createWarning(ObservationManager.bundle.getString("error.noStatisticsData"));
            return;
        }

        if (this.extLoader.getExtensions().isEmpty()) {
            this.createInfo(ObservationManager.bundle.getString("info.noCatalogsInstalled"));
            return;
        }

        new StatisticsDialog(this);

    }

    private void showExtensionInfo() {

        if (this.extLoader.getExtensions().isEmpty()) {
            this.createInfo(ObservationManager.bundle.getString("info.noExtensionsInstalled"));
        } else {
            new ExtensionInfoDialog(this);
        }

    }

    public void reloadLanguage() {

        // Load new bundle
        this.loadLanguage();

        // Reload title
        this.setTitle();

        // Remove old UI components
        this.hSplitPane.removeAll();
        this.vSplitPane.removeAll();
        super.getContentPane().removeAll();

        // Tell the extensions about the switch
        this.extLoader.reloadLanguage();

        // (Re-)init UI components (would be better to do this with
        // eventing...maybe in a later version :) )
        AbstractDialog.reloadLanguage();
        this.initMenuBar();
        this.item.reloadLanguage();
        this.item = this.initItemView();
        this.table.reloadLanguage();
        this.table = this.initTableView();
        this.tree = this.initTreeView();

        // Rebuild UI
        this.initMain();

        // Reload items
        this.table.showObservations(null, null);
        this.tree.updateTree();

    }

    private void showPreferencesDialog() {

        new PreferencesDialog(this, this.extLoader.getPreferencesTabs());

    }

    private void showDidYouKnow() {

        new DidYouKnowDialog(this);

    }

    private void showLogDialog() {

        new LogDialog(this, this.logFile);

    }

    /*
     * public void createHTMLForObservation(IObservation obs) {
     *
     * // Get DOM source Document doc =
     * this.xmlCache.getDocumentForObservation(obs);
     *
     * // XML File needs to be saved, as otherwise we don't get the path String[]
     * files = this.xmlCache.getAllOpenedFiles(); if( (files == null) ||
     * (files.length == 0) ) { // There is data (otherwise we wouldn't have come
     * here), but data's not saved this.createInfo(ObservationManager.bundle
     * .getString("error.noXMLFileOpen")); return; } // @todo This works only with
     * ONE file opened File xmlFile = new File(files[0]); // Get filename Calendar
     * begin = obs.getBegin(); String htmlName = "" + begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * this.replaceSpecialChars(obs.getTarget().getName()); String fullFileName =
     * xmlFile.getParent() + File.separatorChar + htmlName + ".html"; File html =
     * new File(fullFileName); int i=2; while( html.exists() ) { // Check if file
     * exists (Two observations (at same time) from different user, eyepieces,
     * scopes, ... fullFileName = xmlFile.getParent() + File.separatorChar +
     * htmlName + "(" + i +").html"; i++; html = new File(fullFileName); }
     *
     * this.transformXML2HTML(doc, html);
     *
     * }
     *
     * public void createHTMLForSession(ISession session) {
     *
     * // Get DOM source Document doc =
     * this.xmlCache.getDocumentForSession(session);
     *
     * // XML File needs to be saved, as otherwise we don't get the path String[]
     * files = this.xmlCache.getAllOpenedFiles(); if( (files == null) ||
     * (files.length == 0) ) { // There is data (otherwise we wouldn't have come
     * here), but data's not saved this.createInfo(ObservationManager.bundle
     * .getString("error.noXMLFileOpen")); return; } // @todo This works only with
     * ONE file opened File xmlFile = new File(files[0]); // Get filename Calendar
     * begin = session.getBegin(); String htmlName = "" + begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName(); String fullFileName = xmlFile.getParent() +
     * File.separatorChar + htmlName + ".html"; File html = new File(fullFileName);
     * int i=2; while( html.exists() ) { // Check if file exists (Two session (at
     * same time) from different user... fullFileName = xmlFile.getParent() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.transformXML2HTML(doc, html);
     *
     * }
     */

    /*
     * public void createHTMLForSession(ISession session) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForObservation(IObservation obs) {
     *
     * // Build filename Calendar begin = obs.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * this.replaceSpecialChars(obs.getTarget().getName()); String fullFileName =
     * this.getCurrentXMLParentPath() + File.separatorChar + htmlName + ".html";
     * File html = new File(fullFileName); int i=2; while( html.exists() ) { //
     * Check if file exists (Two observations (at same time) from different user,
     * eyepieces, scopes, ... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(obs, html);
     *
     * }
     *
     * public void createHTMLForEyepiece(IEyepiece eyepiece) {
     *
     * // Build filename String model = eyepiece.getModel(); String focalLength = ""
     * + eyepiece.getFocalLength();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * model + focalLength + ".html";
     *
     * this.createHTMLForSchemaElement(eyepiece, new File(fullFileName));
     *
     * }
     *
     * public void createHTMLForSite(ISite site) {
     *
     * site.getDisplayName()
     *
     * // Build filename String name = site.getName(); String focalLength = "" +
     * eyepiece.getFocalLength();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * model + focalLength + ".html";
     *
     * this.createHTMLForSchemaElement(eyepiece, new File(fullFileName));
     *
     * }
     *
     * public void createHTMLForScope(IScope scope) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForFilter(IFilter filter) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForLens(ILens lens) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForObserver(IObserver observer) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForImager(IImager imager) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     *
     * public void createHTMLForTarget(ITarget target) {
     *
     * // Build filename Calendar begin = session.getBegin(); String htmlName = "" +
     * begin.get(Calendar.YEAR) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MONTH)+1) +
     * DateConverter.setLeadingZero(begin.get(Calendar.DAY_OF_MONTH)) + "_" +
     * DateConverter.setLeadingZero(begin.get(Calendar.HOUR_OF_DAY)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.MINUTE)) +
     * DateConverter.setLeadingZero(begin.get(Calendar.SECOND)) + "_" +
     * session.getSite().getName();
     *
     * String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar +
     * htmlName + ".html"; File html = new File(fullFileName); int i=2; while(
     * html.exists() ) { // Check if file exists (Two session (at same time) from
     * different user... fullFileName = this.getCurrentXMLParentPath() +
     * File.separatorChar + htmlName + "(" + i +").html"; i++; html = new
     * File(fullFileName); }
     *
     * this.createHTMLForSchemaElement(session, html);
     *
     * }
     */

    public void createHTMLForSchemaElement(ISchemaElement schemaElement) {

        // Build filename
        String htmlName = schemaElement.getDisplayName();
        htmlName = this.replaceSpecialChars(htmlName);

        String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar + htmlName + ".html";
        File html = new File(fullFileName);
        int i = 2;
        while (html.exists()) { // Check if file exists (e.g. Two session or
                                // observations (at same (start) time) from
                                // different users...
            fullFileName = this.getCurrentXMLParentPath() + File.separatorChar + htmlName + "(" + i + ").html";
            i++;
            html = new File(fullFileName);
        }

        this.createHTMLForSchemaElement(schemaElement, html);

    }

    public void createXMLForSchemaElement(ISchemaElement schemaElement) {

        // Build filename
        String xmlName = schemaElement.getDisplayName();
        xmlName = this.replaceSpecialChars(xmlName);

        String fullFileName = this.getCurrentXMLParentPath() + File.separatorChar + xmlName + ".xml";
        File xml = new File(fullFileName);
        int i = 2;
        while (xml.exists()) { // Check if file exists (e.g. Two session or
                               // observations (at same (start) time) from
                               // different users...
            fullFileName = this.getCurrentXMLParentPath() + File.separatorChar + xmlName + "(" + i + ").xml";
            i++;
            xml = new File(fullFileName);
        }

        this.createXMLForSchemaElement(schemaElement, xml);

    }

    private void createHTML() {

        this.createHTML(null, null, null);

    }

    public void createHTML(Document doc, File html, File xslFile) {

        if (doc == null) {
            doc = xmlCache.getDocument();
        }

        String[] files = xmlCache.getAllOpenedFiles();
        if ((files == null) || (files.length == 0)) {
            createInfo(bundle.getString("error.noXMLFileOpen"));
            return;
        }

        if (html == null) {
            File xmlFile = new File(files[0]);
            String htmlName = xmlFile.getName();
            htmlName = htmlName.substring(0, htmlName.indexOf('.'));
            htmlName = xmlFile.getParent() + File.separatorChar + htmlName + ".html";
            html = new File(htmlName);
        }

        boolean result = this.transformXML2HTML(doc, html, xslFile);
        if (result) {
            this.createInfo(ObservationManager.bundle.getString("info.htmlExportDir") + " " + html);
        } // Otherwise error message have been provided

    }

    private void createNewObservation() {

        ObservationDialog dialog = null;
        while (dialog == null || dialog.isCreateAdditionalObservation()) {
            dialog = new ObservationDialog(this, null);
            this.xmlCache.addSchemaElement(dialog.getObservation());
            this.updateLeft(); // Refreshes tree (without that, the new element
                               // won't appear on UI)
            this.updateUI(dialog.getObservation()); // Sets selection in tree
                                                    // (and table) on new
                                                    // element
        }

    }

    private void createNewObserver() {

        ObserverDialog dialog = new ObserverDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getObserver());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getObserver()); // Sets selection in tree (and
                                             // table) on new element

    }

    private void createNewSession() {

        SessionDialog dialog = new SessionDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getSession());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getSession()); // Sets selection in tree (and
                                            // table) on new element

    }

    private void createNewSite() {

        SiteDialog dialog = new SiteDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getSite());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getSite()); // Sets selection in tree (and table)
                                         // on new element

    }

    private void createNewScope() {

        ScopeDialog dialog = new ScopeDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getScope());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getScope()); // Sets selection in tree (and table)
                                          // on new element

    }

    private void createNewEyepiece() {

        EyepieceDialog dialog = new EyepieceDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getEyepiece());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getEyepiece()); // Sets selection in tree (and
                                             // table) on new element

    }

    private void createNewImager() {

        ExtenableSchemaElementSelector is = new ExtenableSchemaElementSelector(this, this.extLoader.getSchemaUILoader(),
                SchemaElementConstants.IMAGER);
        if (is.getResult()) {
            // Get Imager Dialog
            IImagerDialog imagerDialog = (IImagerDialog) is.getDialog();
            this.xmlCache.addSchemaElement(imagerDialog.getImager());
            this.updateLeft(); // Refreshes tree (without that, the new element
                               // won't appear on UI)
            this.updateUI(imagerDialog.getImager()); // Sets selection in tree
                                                     // (and table) on new
                                                     // element
        }

    }

    private void createNewFilter() {

        FilterDialog dialog = new FilterDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getFilter());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getFilter()); // Sets selection in tree (and table)
                                           // on new element

    }

    private void createNewTarget() {

        ExtenableSchemaElementSelector ts = new ExtenableSchemaElementSelector(this, this.extLoader.getSchemaUILoader(),
                SchemaElementConstants.TARGET);
        if (ts.getResult()) {
            // Get TargetContainer
            ITargetDialog targetDialog = (ITargetDialog) ts.getDialog();
            this.xmlCache.addSchemaElement(targetDialog.getTarget());
            this.updateLeft(); // Refreshes tree (without that, the new element
                               // won't appear on UI)
            this.updateUI(targetDialog.getTarget()); // Sets selection in tree
                                                     // (and table) on new
                                                     // element
        }

    }

    private void createNewLens() {

        LensDialog dialog = new LensDialog(this, null);
        this.xmlCache.addSchemaElement(dialog.getLens());
        this.updateLeft(); // Refreshes tree (without that, the new element
                           // won't appear on UI)
        this.updateUI(dialog.getLens()); // Sets selection in tree (and table)
                                         // on new element

    }

    public void deleteSchemaElement(ISchemaElement element) {

        if (element == null) {
            return;
        }

        // Confirmation pop-up
        JOptionPane pane = new JOptionPane(ObservationManager.bundle.getString("info.delete.question"),
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(this, ObservationManager.bundle.getString("info.delete.title"));
        dialog.setVisible(true);
        Object selectedValue = pane.getValue();
        if ((selectedValue instanceof Integer)) {
            if ((Integer) selectedValue == JOptionPane.NO_OPTION) {
                return; // don't delete
            }
        }

        List result = this.xmlCache.removeSchemaElement(element);
        if (result == null) { // Deletion failed
            if (element instanceof ITarget) {
                this.createWarning(ObservationManager.bundle.getString("error.deleteTargetFromCatalog"));
                return;
            }
            System.err.println("Error during deletion of element: " + element);
            return;
        }

        if (result.isEmpty()) { // Deletion successful
            this.setChanged(true);
            this.update(element);
        } else { // Deletion failed due to dependencies
            new TableElementsDialog(this, result);
        }

    }

    public void loadFiles(String[] files) {

        if ((files == null) || (files.length == 0)) {
            return;
        }

        for (String file : files) {
            this.loadFile(file);
        }

    }

    private void loadFile(final String file) {

        if (file == null) {
            return;
        }

        this.cleanUp();

        if (this.debug) {
            System.out.println("Load File: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

        Worker calculation = new Worker() {

            private String message;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            @Override
            public void run() {

                boolean result = ObservationManager.this.xmlCache.loadObservations(file);
                if (!result) {
                    message = ObservationManager.bundle.getString("error.loadXML") + " " + file;
                    returnValue = Worker.RETURN_TYPE_ERROR;
                }

                ObservationManager.this.table.showObservations(null, null);
                ObservationManager.this.tree.updateTree();

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        };

        // This should avoid some nasty ArrayIndexOutOfBoundsExceptions which
        // are
        // thrown time by time at the ProgressDialog.setVisible(true) call.
        // Problems seems that the DefaultTableModelRenderer tries to update a
        // certain
        // part of the screen while the ProgressDialogs calculation thread is
        // currently
        // loading the XML file. This seems to cause the problem. Clearing the
        // table like
        // below, seems to fix this strange problem
        this.table.showObservations(null, null);

        new ProgressDialog(this, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.xml.load.info"), calculation);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }
        } else {
            this.createWarning(calculation.getReturnMessage());
        }

        if (this.debug) {
            System.out.println("Loaded: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

    }

    private void loadFile(File file) {

        if (file == null) {
            return;
        }

        this.loadFile(file.getAbsolutePath());

    }

    public XMLFileLoader getXmlCache() {

        return this.xmlCache;

    }

    /*
     * public SchemaUILoader getLoader() {
     *
     * return this.loader;
     *
     * }
     *
     * public CatalogLoader getCatalogLoader() {
     *
     * return this.catLoader;
     *
     * }
     */

    public ExtensionLoader getExtensionLoader() {

        return this.extLoader;

    }

    public ISchemaElement getSelectedTableElement() {

        return this.table.getSelectedElement();

    }

    // parentElement can be null (in that case all available observations will
    // be shown)
    public void updateRight(ISchemaElement element, ISchemaElement parentElement) {

        if (element != null) {
            // calling showObservations on table is sufficient, as this
            // internally calls showObservation on the itemView.

            if (element instanceof IObservation) {
                // this.item.showObservation((IObservation)element);
                this.table.showObservations((IObservation) element, parentElement);
            } else if (element instanceof ITarget) {
                // this.item.showTarget((ITarget)element);
                this.table.showTargets((ITarget) element);
            } else if (element instanceof IScope) {
                // this.item.showScope((IScope)element);
                this.table.showScopes((IScope) element);
            } else if (element instanceof IEyepiece) {
                // this.item.showEyepiece((IEyepiece)element);
                this.table.showEyepieces((IEyepiece) element);
            } else if (element instanceof IImager) {
                // this.item.showImager((IImager)element);
                this.table.showImagers((IImager) element);
            } else if (element instanceof IFilter) {
                // this.item.showImager((IFilter)element);
                this.table.showFilters((IFilter) element);
            } else if (element instanceof ISite) {
                // this.item.showSite((ISite)element);
                this.table.showSites((ISite) element);
            } else if (element instanceof ISession) {
                // this.item.showSession((ISession)element);
                this.table.showSessions((ISession) element);
            } else if (element instanceof IObserver) {
                // this.item.showObserver((IObserver)element);
                this.table.showObservers((IObserver) element);
            } else if (element instanceof ILens) {
                // this.item.showLens((ILens)element);
                this.table.showLenses((ILens) element);
            }
        }

    }

    public void updateLeft() {

        this.tree.updateTree();

    }

    public void updateUI(ISchemaElement element) {

        // Update UI
        this.tree.setSelection(element, null); // This is enough...the rest
                                               // (table, item) will be updated
                                               // subsequently

    }

    public void update(ISchemaElement element) {

        // Update cache
        this.xmlCache.updateSchemaElement(element);

        // Update tree (clears old data and refreshes it completely)
        this.updateLeft();

        // Update UI
        this.updateUI(element);

    }

    public void setChanged(boolean changed) {

        if ((changed) // From unchanged to changed
                && (!this.changed)) {
            this.setTitle(this.getTitle() + " *");
        } else if (!changed) {
            this.setTitle(); // From changed to unchanged
        }
        this.changed = changed;

    }

    public ItemView getItemView() {

        return this.item;

    }

    public TableView getTableView() {

        return this.table;

    }

    public TreeView getTreeView() {

        return this.tree;

    }

    public JSplitPane getHorizontalSplitPane() {

        return this.hSplitPane;

    }

    public JSplitPane getVerticalSplitPane() {

        return this.vSplitPane;

    }

    public void createWarning(String message) {

        JOptionPane.showMessageDialog(this, message, ObservationManager.bundle.getString("title.warning"),
                JOptionPane.WARNING_MESSAGE);

    }

    public void createInfo(String message) {

        JOptionPane.showMessageDialog(this, message, ObservationManager.bundle.getString("title.info"),
                JOptionPane.INFORMATION_MESSAGE);

    }

    public Configuration getConfiguration() {

        return this.configuration;

    }

    public boolean isDebug() {

        return this.debug;

    }

    public boolean isNightVisionEnabled() {

        return this.nightVision.isSelected();

    }

    public ProjectCatalog[] getProjects() {

        // Wait for ProjectLoader to finish
        if (this.waitForCatalogLoaderThread.isAlive()) {
            try {
                this.waitForCatalogLoaderThread.join();
            } catch (InterruptedException ie) {
                System.err.println(
                        "Got interrupted while waiting for catalog loader...List of projects will be empty. Please try again.");
                return null;
            }
        }

        return this.projectLoader.getProjects();

    }

    public void resetWindowSizes() {

        // Get all keys that represent a window size
        Iterator keyIterator = this.getConfiguration().getConfigKeys().iterator();
        String currentKey;
        ArrayList removeKeys = new ArrayList();
        while (keyIterator.hasNext()) {
            currentKey = (String) keyIterator.next();
            if (currentKey.startsWith(OMDialog.DIALOG_SIZE_KEY)) {
                removeKeys.add(currentKey);
            }
        }

        // Delete all window size information
        for (Object removeKey : removeKeys) {
            this.getConfiguration().setConfig((String) removeKey, null);
        }

    }

    private UpdateChecker checkForUpdates() {

        // The updateChecker
        UpdateChecker updateChecker = new UpdateChecker(this);

        if (true) {
            updateChecker.run();
        } else {
            Thread updateThread = new Thread(updateChecker, "Check for Updates");
            updateThread.start();
        }

        return updateChecker;

    }

    /*
     * public void updateInstallation(List updateEntries) {
     *
     * if( updateEntries.isEmpty() ) { return; }
     *
     * // Download files into download folder underneath configDir File directory =
     * new File(this.configuration.getConfigPath(this.configDir) + File.separator +
     * "download"); if( !directory.exists() ) { directory.mkdir(); }
     *
     * // Download files boolean result = this.downloadFiles(updateEntries,
     * directory);
     *
     * if( result == true ) {
     */// Download was successfull

    // ------------------------------------------------------------------------------------------
    // Auto. Updater doesn't work, as long as we cannot update Classloaders
    // during runtime.
    // Every change in ObservationManager.class would break the autom. update,
    // as after unzipping
    // the new version every new call will result in a ClassNotFoundException
    // Therefore we can only provide semi automatic update at this time.
    // ------------------------------------------------------------------------------------------

    // Get ObservationManager main file
    /*
     * File[] omFiles = directory.listFiles(new FilenameFilter() {
     *
     * public boolean accept(File dir, String name) {
     *
     * if( (name.endsWith(".zip")) && (name.startsWith("observationManager")) ) {
     * return true; } return false; }
     *
     * });
     *
     * // Create ZIP file ZipFile zf = null; try { zf = new ZipFile(omFiles[0]); }
     * catch( IOException ioe ) { System.err.println("Unable to access zip file: " +
     * omFiles[0]); }
     *
     *
     * // Check whether we've write access in current installation to all files from
     * zip file if( this.checkWriteAccess(zf, this.installDir, true) ) {
     * System.out.println("Start update of Observation Manager");
     *
     *
     * // --- Observation Manager
     *
     * // Do actual installation boolean omResult = true; // Initialize with TRUE as
     * main file might not require update boolean extensionResult = true; //
     * Initialize with TRUE as no extensions require update if( (omFiles != null) &&
     * (omFiles.length > 0) ) { omResult = this.updateObservationManager(zf); }
     *
     * // Delete OM file if( omResult && omFiles.length > 0 ) { // There was an OM
     * update System.out.println("Update of Observation Manager successful.");
     * omFiles[0].delete();
     *
     * // Check whether there are also extensions to update if(
     * this.getDownloadedOMEFiles(directory).length > 0 ) { // Mark the next start
     * of OM as a "restart" update -> OME should try to autoinstall files
     * this.configuration.setConfig(ObservationManager.CONFIG_UPDATE_RESTART,
     * "true"); this.createInfo(
     * "Update of ObservationManager successfull! Restart of Observation Manager required.\nExtensions will be updated automatically during next start of ObservationManager."
     * ); System.out.println(
     * "Update extensions during next startup. Shutdown ObservationManager now." );
     * } else { this.createInfo(
     * "Update of ObservationManager successfull! Restart of Observation Manager required."
     * ); }
     *
     * this.exit(); } else { // OM update failed System.err.println(
     * "Update of ObservationManager failed. Please see log for details.");
     * this.createWarning(
     * "Update of ObservationManager failed. Please see log for details."); return;
     * }
     *
     *
     *
     * // --- Extensions
     *
     * // Only install extensions if Observation Manager update was OK File[]
     * extFiles = this.getDownloadedOMEFiles(directory);
     *
     * // Install extensions without prompting for restart if( (extFiles != null) &&
     * (extFiles.length > 0) ) { extensionResult = this.installExtension(extFiles,
     * false); }
     *
     * if( extensionResult && extFiles.length > 0 ) { // Update was successful
     * this.createInfo(
     * "Update of Extensions successfull! Restart of Observation Manager required."
     * ); System.out.println("Update of Extensions successful.");
     *
     * // Delete OME files for(int i=0; i < extFiles.length; i++) {
     * extFiles[i].delete(); }
     *
     * this.exit(); } else { // Update failed
     * System.err.println("Extension update failed."); this.createInfo(
     * "Automatic update of Extensions not possible. Please update Extensions manually.\nExtension file(s) downloaded to: "
     * + directory.getAbsolutePath()); }
     *
     * } else { // We don't have all required permissions
     */
    // this.createInfo("File(s) downloaded to: " + directory.getAbsolutePath() +
    // "\nPlease install manually.");
    // }
    /*
     * } else { this.createWarning(
     * "Problem while retrieving file(s). Please see log for details."); }
     */
    // }

    private boolean checkWriteAccess(ZipFile zipFile, File destinationRoot) {

        Enumeration enumeration = zipFile.entries();
        ZipEntry ze;

        // Unpack all the ZIP file entries into install dir
        File currentFile;
        boolean result = true;
        while (enumeration.hasMoreElements()) {

            ze = (ZipEntry) enumeration.nextElement();
            currentFile = this.getDestinationFile(ze.getName(), destinationRoot, false);

            if (currentFile != null) {
                while (!currentFile.exists()) { // New file/folder, which
                                                // doesn't exist so far
                    currentFile = new File(currentFile.getParent()); // Check
                                                                     // write
                                                                     // permission
                                                                     // on
                                                                     // parent
                }

                if (!currentFile.canWrite()) { // We've found at least one file,
                                               // which we would need to
                                               // overwrite, but do not
                                               // have the permission to
                    System.err.println("Write check failed for: " + currentFile);
                    result = false;
                }
            }

        }

        return result;

    }

    public boolean checkWriteAccess(File file) {

        return file.canWrite();

    }

    // ---------------
    // Private Methods --------------------------------------------------------
    // ---------------

    // ****************************************************************
    // Only required for Auto. Updater, which is currently not supported
    // Intension is here to update the new extensions during restart after
    // Observation Manager has
    // been automatically updated.
    // ****************************************************************

    /*
     * private void performRestartUpdate() {
     *
     * boolean restartUpdate =
     * Boolean.valueOf(this.configuration.getConfig(ObservationManager
     * .CONFIG_UPDATE_RESTART)).booleanValue();
     *
     * if( restartUpdate ) { System.out.println(
     * "--- This is a restart update. Perform remaining update steps now...");
     *
     * // Remove restart required flag
     * this.configuration.setConfig(ObservationManager.CONFIG_UPDATE_RESTART, null);
     */
    /*
     * // Check whether we've extensions to install File directory = new
     * File(this.configuration.getConfigPath(this.configDir) + File.separator +
     * "download"); File[] omeFiles = this.getDownloadedOMEFiles(directory);
     *
     * boolean extensionResult = this.installExtension(omeFiles, false);
     *
     * if( extensionResult && omeFiles.length > 0 ) { // Update was successful
     * this.createInfo(
     * "Update of Extensions successfull! Restart of Observation Manager required again."
     * ); System.out.println("Update of Extensions successful.");
     *
     * // Delete OME files for(int i=0; i < omeFiles.length; i++) {
     * omeFiles[i].delete(); }
     *
     * // Restart OM this.exit(); } else { // Update failed
     * System.err.println("Extension updte failed."); this.createInfo(
     * "Automatic update of Extensions not possible. Please update Extensions manually.\nExtension file(s) downloaded to: "
     * + directory.getAbsolutePath()); }
     */
    /*
     * }
     *
     * }
     */

    private File getDestinationFile(String filename, File destinationFolder, boolean removeRootFolder) {

        if (removeRootFolder) {
            // Remove root folder
            filename = filename.substring(filename.indexOf("/") + 1);

            if ("".equals(filename)) { // That must have been the root folder
                return null;
            }
        }

        return new File(
                destinationFolder.getAbsolutePath() + File.separator + /* "testing" + File.separator + */filename);

    }

    // ****************************************************************
    // Only required for Auto. Update which is currently not supported
    // ****************************************************************

    /*
     * private File[] getDownloadedOMEFiles(File directory) {
     *
     * // Only install extensions if Observation Manager update was OK File[]
     * extFiles = new File[] {};
     *
     * // Get all extensions we've downloaded extFiles = directory.listFiles(new
     * FilenameFilter() {
     *
     * public boolean accept(File dir, String name) {
     *
     * if( name.endsWith(".ome") ) { return true; } return false; } });
     *
     * return extFiles;
     *
     * }
     */

    /*
     * private boolean updateObservationManager(ZipFile zf) {
     *
     * Enumeration enumeration = zf.entries(); ZipEntry ze = null;
     *
     * // Unpack all the ZIP file entries into install dir while(
     * enumeration.hasMoreElements() ) { ze = (ZipEntry)enumeration.nextElement();
     *
     * InputStream istr = null; try { istr = zf.getInputStream(ze); }
     * catch(IOException ioe) {
     * System.err.println("Unable to open input stream from zip file: " + zf +
     * " for entry: " + ze); return false; } BufferedInputStream bis = new
     * BufferedInputStream(istr);
     *
     * // Get destination file annd remove root Folder // Installation zip will have
     * /observationManager as root folder. Need to remove that File file =
     * this.getDestinationFile(ze.getName(), this.getInstallDir(), true); if( file
     * == null ) { continue; }
     *
     * // Create new directories if( ze.isDirectory() ) { if( !file.exists() ) { //
     * Directory doesn't exist already boolean createDir = false; createDir =
     * file.mkdir(); if( !createDir ) {
     * System.err.println("Unable to create directory: " + file); return false; }
     * else { continue; } } else { continue; } }
     *
     * // Update files FileOutputStream fos = null; try { fos = new
     * FileOutputStream(file); } catch(FileNotFoundException fnfe) {
     * System.err.println("Unable to create file: " + file + "\n" + fnfe); return
     * false; } int sz = (int)ze.getSize(); final int N = 1024; byte buf[] = new
     * byte[N]; int ln = 0; try { while( (sz > 0) // workaround for bug && ((ln =
     * bis.read(buf, 0, Math.min(N, sz))) != -1) ) { fos.write(buf, 0, ln); sz -=
     * ln; } bis.close(); fos.flush(); } catch(IOException ioe) {
     * System.err.println("Unable to write file: " + ze + "\n" + ioe); return false;
     * }
     *
     * }
     *
     * return true;
     *
     * }
     */

    private void loadConfig() {

        // Check if we should load last loaded XML on startup
        boolean load = Boolean.parseBoolean(this.configuration.getConfig(ObservationManager.CONFIG_OPENONSTARTUP));
        if (load) {
            String lastFile = this.configuration.getConfig(ObservationManager.CONFIG_LASTXML);
            // Check if last file is set
            if ((lastFile != null) && !("".equals(lastFile))) {
                this.loadFile(new File(lastFile));
            }
        }

    }

    private File saveDialog() {

        JFileChooser chooser = new JFileChooser();

        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnValue = chooser.showSaveDialog(this);
        File file = null;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        if ((file != null) && (!file.getName().toLowerCase().endsWith(".xml"))) {
            file = new File(file.getAbsolutePath() + ".xml");
        }

        return file;

    }

    private void cleanUp() {

        this.xmlCache.clear();
        this.tree.updateTree();
        this.uiDataCache.clear();

    }

    private void loadLanguage() {

        // Locale.default might be already set by parseArguments

        // Try to find value in config
        String isoKey = this.configuration.getConfig(ObservationManager.CONFIG_UILANGUAGE);
        if (isoKey != null) {
            Locale.setDefault(new Locale(isoKey, isoKey));
            System.setProperty("user.language", isoKey);
            System.setProperty("user.region", isoKey);
            JComponent.setDefaultLocale(Locale.getDefault());
        }

        try {
            ObservationManager.bundle = (PropertyResourceBundle) ResourceBundle.getBundle("ObservationManager",
                    Locale.getDefault());
        } catch (MissingResourceException mre) { // Unknown VM language (and
                                                 // language not explicitly
                                                 // set)
            Locale.setDefault(Locale.ENGLISH);
            ObservationManager.bundle = (PropertyResourceBundle) ResourceBundle.getBundle("ObservationManager",
                    Locale.getDefault());
        }

    }

    private void setTitle() {

        Class toolkit = Toolkit.getDefaultToolkit().getClass();
        if (toolkit.getName().equals("sun.awt.X11.XToolkit")) { // Sets title
                                                                // correct in
                                                                // Linux/Gnome3
                                                                // desktop
            try {
                Field awtAppClassName = toolkit.getDeclaredField("awtAppClassName");
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, "Observation Manager - " + ObservationManager.bundle.getString("version")
                        + " " + ObservationManager.VERSION);
            } catch (Exception e) {
                // Cannot do much here
            }
        }

        super.setTitle("Observation Manager - " + ObservationManager.bundle.getString("version") + " "
                + ObservationManager.VERSION);

    }

    private void initMenuBar() {

        String iconDir = this.installDir.getPathForFolder("images");
        int menuKeyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        this.menuBar = new JMenuBar();

        // ----- File Menu
        JMenu fileMenu = new JMenu(ObservationManager.bundle.getString("menu.file"));
        fileMenu.setMnemonic('f');
        this.menuBar.add(fileMenu);

        this.newFile = new JMenuItem(ObservationManager.bundle.getString("menu.newFile"),
                new ImageIcon(iconDir + "newDocument.png"));
        this.newFile.setMnemonic('n');
        this.newFile.addActionListener(this);
        fileMenu.add(newFile);

        this.openFile = new JMenuItem(ObservationManager.bundle.getString("menu.openFile"),
                new ImageIcon(iconDir + "open.png"));
        this.openFile.setMnemonic('o');
        this.openFile.addActionListener(this);
        this.openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuKeyModifier));
        fileMenu.add(openFile);

        // this.openDir = new JMenuItem("Open dir");
        // this.openDir.setMnemonic('d');
        // this.openDir.addActionListener(this);
        // this.fileMenu.add(openDir); // @todo: Uncomment this as soon as we
        // know what this means

        this.saveFile = new JMenuItem(ObservationManager.bundle.getString("menu.save"),
                new ImageIcon(iconDir + "save.png"));
        this.saveFile.setMnemonic('s');
        this.saveFile.addActionListener(this);
        this.saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuKeyModifier));
        fileMenu.add(saveFile);

        this.saveFileAs = new JMenuItem(ObservationManager.bundle.getString("menu.saveAs"),
                new ImageIcon(iconDir + "save.png"));
        this.saveFileAs.setMnemonic('a');
        this.saveFileAs.addActionListener(this);
        fileMenu.add(saveFileAs);

        fileMenu.addSeparator();

        this.importXML = new JMenuItem(ObservationManager.bundle.getString("menu.xmlImport"),
                new ImageIcon(iconDir + "importXML.png"));
        this.importXML.setMnemonic('i');
        this.importXML.addActionListener(this);
        fileMenu.add(importXML);

        fileMenu.addSeparator();

        this.exportHTML = new JMenuItem(ObservationManager.bundle.getString("menu.htmlExport"),
                new ImageIcon(iconDir + "export.png"));
        this.exportHTML.setMnemonic('e');
        this.exportHTML.addActionListener(this);
        this.exportHTML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuKeyModifier));
        fileMenu.add(exportHTML);

        fileMenu.addSeparator();

        this.exit = new JMenuItem(ObservationManager.bundle.getString("menu.exit"),
                new ImageIcon(iconDir + "exit.png"));
        this.exit.setMnemonic('x');
        this.exit.addActionListener(this);
        fileMenu.add(exit);

        // ----- Data Menu
        JMenu dataMenu = new JMenu(ObservationManager.bundle.getString("menu.data"));
        dataMenu.setMnemonic('d');
        this.menuBar.add(dataMenu);

        this.createObservation = new JMenuItem(ObservationManager.bundle.getString("menu.createObservation"),
                new ImageIcon(iconDir + "observation_l.png"));
        this.createObservation.setMnemonic('o');
        this.createObservation.addActionListener(this);
        this.createObservation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuKeyModifier));
        dataMenu.add(createObservation);

        // Seperate Observation from the rest
        dataMenu.addSeparator();

        this.createObserver = new JMenuItem(ObservationManager.bundle.getString("menu.createObserver"),
                new ImageIcon(iconDir + "observer_l.png"));
        this.createObserver.setMnemonic('v');
        this.createObserver.addActionListener(this);
        dataMenu.add(createObserver);

        this.createSite = new JMenuItem(ObservationManager.bundle.getString("menu.createSite"),
                new ImageIcon(iconDir + "site_l.png"));
        this.createSite.setMnemonic('l');
        this.createSite.addActionListener(this);
        dataMenu.add(createSite);

        this.createScope = new JMenuItem(ObservationManager.bundle.getString("menu.createScope"),
                new ImageIcon(iconDir + "scope_l.png"));
        this.createScope.setMnemonic('s');
        this.createScope.addActionListener(this);
        dataMenu.add(createScope);

        this.createEyepiece = new JMenuItem(ObservationManager.bundle.getString("menu.createEyepiece"),
                new ImageIcon(iconDir + "eyepiece_l.png"));
        this.createEyepiece.setMnemonic('e');
        this.createEyepiece.addActionListener(this);
        dataMenu.add(createEyepiece);

        this.createLens = new JMenuItem(ObservationManager.bundle.getString("menu.createLens"),
                new ImageIcon(iconDir + "lens_l.png"));
        this.createLens.setMnemonic('o');
        this.createLens.addActionListener(this);
        dataMenu.add(createLens);

        this.createFilter = new JMenuItem(ObservationManager.bundle.getString("menu.createFilter"),
                new ImageIcon(iconDir + "filter_l.png"));
        this.createFilter.setMnemonic('f');
        this.createFilter.addActionListener(this);
        dataMenu.add(createFilter);

        this.createImager = new JMenuItem(ObservationManager.bundle.getString("menu.createImager"),
                new ImageIcon(iconDir + "imager_l.png"));
        this.createImager.setMnemonic('i');
        this.createImager.addActionListener(this);
        dataMenu.add(createImager);

        this.createTarget = new JMenuItem(ObservationManager.bundle.getString("menu.createTarget"),
                new ImageIcon(iconDir + "target_l.png"));
        this.createTarget.setMnemonic('t');
        this.createTarget.addActionListener(this);
        dataMenu.add(createTarget);

        this.createSession = new JMenuItem(ObservationManager.bundle.getString("menu.createSession"),
                new ImageIcon(iconDir + "session_l.png"));
        this.createSession.setMnemonic('n');
        this.createSession.addActionListener(this);
        dataMenu.add(createSession);

        // Seperate Availability from the rest
        dataMenu.addSeparator();

        this.equipmentAvailability = new JMenuItem(ObservationManager.bundle.getString("menu.equipmentAvailability"),
                new ImageIcon(iconDir + "equipment.png"));
        this.equipmentAvailability.setMnemonic('a');
        this.equipmentAvailability.addActionListener(this);
        dataMenu.add(equipmentAvailability);

        // ----- Extras Menu
        JMenu extraMenu = new JMenu(ObservationManager.bundle.getString("menu.extra"));
        extraMenu.setMnemonic('e');
        this.menuBar.add(extraMenu);

        this.showStatistics = new JMenuItem(ObservationManager.bundle.getString("menu.showStatistics"),
                new ImageIcon(iconDir + "statistic.png"));
        this.showStatistics.setMnemonic('s');
        this.showStatistics.addActionListener(this);
        extraMenu.add(showStatistics);

        this.preferences = new JMenuItem(ObservationManager.bundle.getString("menu.preferences"),
                new ImageIcon(iconDir + "preferences.png"));
        this.preferences.setMnemonic('p');
        this.preferences.addActionListener(this);
        extraMenu.add(preferences);

        extraMenu.addSeparator();

        this.didYouKnow = new JMenuItem(ObservationManager.bundle.getString("menu.didYouKnow"),
                new ImageIcon(iconDir + "questionMark.png"));
        this.didYouKnow.setMnemonic('d');
        this.didYouKnow.addActionListener(this);
        this.didYouKnow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        extraMenu.add(didYouKnow);

        extraMenu.addSeparator();

        this.nightVision = new JCheckBoxMenuItem(ObservationManager.bundle.getString("menu.nightVision"));
        this.nightVision.setMnemonic('v');
        this.nightVision.addActionListener(this);
        extraMenu.add(nightVision);

        extraMenu.addSeparator();

        this.logMenuEntry = new JMenuItem(ObservationManager.bundle.getString("menu.log"),
                new ImageIcon(iconDir + "logviewer.png"));
        this.logMenuEntry.setMnemonic('l');
        this.logMenuEntry.addActionListener(this);
        extraMenu.add(logMenuEntry);

        extraMenu.addSeparator();

        this.updateMenuEntry = new JMenuItem(ObservationManager.bundle.getString("menu.updateCheck"),
                new ImageIcon(iconDir + "updater.png"));
        this.updateMenuEntry.setMnemonic('u');
        this.updateMenuEntry.addActionListener(this);
        extraMenu.add(updateMenuEntry);

        // ----- Extensions Menu
        JMenu extensionMenu = new JMenu(ObservationManager.bundle.getString("menu.extension"));
        extensionMenu.setMnemonic('x');
        this.menuBar.add(extensionMenu);

        JMenu[] menus = this.extLoader.getMenus();
        for (JMenu menu : menus) {
            extensionMenu.add(menu);
        }

        if (menus.length != 0) {
            extensionMenu.addSeparator();
        }

        this.extensionInfo = new JMenuItem(ObservationManager.bundle.getString("menu.extensionInfo"),
                new ImageIcon(iconDir + "extensionInfo.png"));
        this.extensionInfo.setMnemonic('p');
        this.extensionInfo.addActionListener(this);
        extensionMenu.add(extensionInfo);

        this.installExtension = new JMenuItem(ObservationManager.bundle.getString("menu.installExtension"),
                new ImageIcon(iconDir + "extension.png"));
        this.installExtension.setMnemonic('i');
        this.installExtension.addActionListener(this);
        extensionMenu.add(installExtension);

        // ----- About Menu
        JMenu aboutMenu = new JMenu(ObservationManager.bundle.getString("menu.about"));
        aboutMenu.setMnemonic('a');
        this.menuBar.add(aboutMenu);

        this.aboutInfo = new JMenuItem(ObservationManager.bundle.getString("menu.aboutOM"),
                new ImageIcon(iconDir + "about.png"));
        this.aboutInfo.setMnemonic('i');
        this.aboutInfo.addActionListener(this);
        aboutMenu.add(aboutInfo);

        this.setJMenuBar(this.menuBar);

    }

    private void initMain() {

        this.setLocationAndSize();

        this.hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.hSplitPane.setTopComponent(this.table);
        this.hSplitPane.setBottomComponent(this.item);
        this.hSplitPane.setContinuousLayout(true);
        super.getContentPane().add(hSplitPane);

        this.vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.vSplitPane.setLeftComponent(this.tree);
        this.vSplitPane.setRightComponent(this.hSplitPane);
        this.vSplitPane.setContinuousLayout(true);
        super.getContentPane().add(vSplitPane);

        this.hSplitPane.setVisible(true);
        this.vSplitPane.setVisible(true);

        this.setDividerLocation();

        // Wait til SplashScreen disappears
        if (this.splash != null) { // In night mode there is no Splash screen
            try {
                this.splash.join();
            } catch (InterruptedException ie) {
                System.out.println("Waiting for SplashScreen interrupted");
            }
        }

        super.setVisible(true);

    }

    private void setLocationAndSize() {

        // Get the size of the screen
        Dimension maxSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Get last size
        String stringSize = this.configuration.getConfig(ObservationManager.CONFIG_MAINWINDOW_SIZE,
                maxSize.width + "x" + maxSize.height);
        int width = Integer.parseInt(stringSize.substring(0, stringSize.indexOf('x')));
        int height = Integer.parseInt(stringSize.substring(stringSize.indexOf('x') + 1));
        if (width > maxSize.width) {
            width = maxSize.width;
        }
        if (height > maxSize.height) {
            height = maxSize.height;
        }
        Dimension size = new Dimension(width, height);
        this.setSize(size);

        // Location
        String stringLocation = this.configuration.getConfig(ObservationManager.CONFIG_MAINWINDOW_POS);
        int x = 0;
        int y = 0;
        if (stringLocation != null && !"".equals(stringLocation.trim())) {
            x = Integer.parseInt(stringLocation.substring(0, stringLocation.indexOf(',')));
            y = Integer.parseInt(stringLocation.substring(stringLocation.indexOf(',') + 1));
            // Check if position is in current screen size
            if (x > maxSize.width) {
                x = 0;
            }
            if (y > maxSize.height) {
                y = 0;
            }
        }
        this.setLocation(x, y);

        // Check if we're maximized the last time, and if so, maximized again
        boolean maximized = Boolean.parseBoolean(
                this.configuration.getConfig(ObservationManager.CONFIG_MAINWINDOW_MAXIMIZED, Boolean.toString(false)));
        if (maximized) {
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

    }

    private void setDividerLocation() {

        // Set dividers
        String sVertical = this.configuration.getConfig(ObservationManager.CONFIG_MAINWINDOW_DIVIDER_VERTICAL);
        String sHorizontal = this.configuration.getConfig(ObservationManager.CONFIG_MAINWINDOW_DIVIDER_HORIZONTAL);

        float vertical = 0;
        float horizontal = 0;
        if ((sHorizontal != null) && (sVertical != null)) {
            try {
                vertical = FloatUtil.parseFloat(sVertical);
                horizontal = FloatUtil.parseFloat(sHorizontal);
            } catch (NumberFormatException nfe) { // In case of errors set
                                                  // default values
                sVertical = null;
                sHorizontal = null;
            }
        }

        if ((sVertical == null) || ("".equals(sVertical.trim()))) {
            this.vSplitPane.setDividerLocation(this.getWidth() / 5);
        } else {
            this.vSplitPane.setDividerLocation((int) (this.getWidth() / vertical));
        }

        if ((sVertical == null) || ("".equals(sVertical.trim()))) {
            this.hSplitPane.setDividerLocation((int) (this.getHeight() / 2.7));
        } else {
            this.hSplitPane.setDividerLocation((int) (this.getHeight() / horizontal));
        }

    }

    private TableView initTableView() {

        TableView table = new TableView(this);
        // table.setMinimumSize(new Dimension(this.getWidth()/2,
        // this.getHeight()));
        table.setVisible(true);

        return table;

    }

    private ItemView initItemView() {

        ItemView item = new ItemView(this);
        // item.setMinimumSize(new Dimension(this.getWidth()/2,
        // this.getHeight()));
        item.setVisible(true);

        return item;

    }

    private TreeView initTreeView() {

        TreeView tree = new TreeView(this);
        tree.setMinimumSize(new Dimension(this.getWidth() / 8, this.getHeight()));
        tree.setVisible(true);

        return tree;

    }

    private Templates getTemplate(StreamSource xslSource) {

        Templates template = null;

        /*
         * *********** AS WE PACK APACHE XALAN WITH OM (since 0.314) we don't need this
         * any loner *************)
         *
         * // Classname (package) changed between JDK1.4 and JDK1.5 // So we check VM
         * version and load the right class via reflection...hopefully String version =
         * System.getProperty("java.version"); // String classname =
         * "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl" ; // >=
         * JDK1.5 String classname = "javax.xml.transform.TransformerFactory"; if(
         * version.startsWith("1.4") ) { String classname =
         * "org.apache.xalan.processor.TransformerFactoryImpl"; // JDK1.4 }
         *
         * // Even if classname changed the class itself if the same, so we need only on
         * block to load the class.... try { Class transformerClass =
         * Class.forName(classname); Method newInstance =
         * transformerClass.getMethod("newInstance", null); Object transformerClassImpl
         * = newInstance.invoke(transformerClass, null); Method newTemplates =
         * transformerClassImpl.getClass().getMethod("newTemplates", new Class[] {
         * Source.class }); template =
         * (Templates)newTemplates.invoke(transformerClassImpl, new StreamSource[] {
         * xslSource }); } catch( ClassNotFoundException cnfe ) {
         * System.out.println("--- Unable to load class: " + classname); } catch(
         * NoSuchMethodException nsme ) { System.out.println("--- Unable to class: " +
         * classname + " looks strange. What JDK version is this? " + version); } catch(
         * IllegalAccessException iae ) {
         * System.out.println("--- Unable to access class: " + classname); } catch(
         * InvocationTargetException ite ) {
         * System.out.println("--- Unable to invoke method on class: " + classname); }
         */

        try {
            template = TransformerFactory.newInstance().newTemplates(xslSource);
        } catch (TransformerConfigurationException tce) {
            System.err.println("--- Unable to get XSLTransformator: " + tce);
        }

        return template;

    }

    private String replaceSpecialChars(String string) {

        string = string.replace('/', '_');
        string = string.replace('\\', '_');
        string = string.replace('@', '_');
        string = string.replace('$', '_');
        string = string.replace('%', '_');
        string = string.replace('&', '_');
        string = string.replace(':', '_');
        string = string.replace(';', '_');

        return string;

    }

    private boolean transformXML2HTML(final Document doc, final File htmlFile, final File xslFile) {

        Worker calculation = new Worker() {

            private String message;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            @Override
            public void run() {

                if ((doc == null) || (htmlFile == null)) {
                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.transformation");
                    return;
                }

                DOMSource source = new DOMSource(doc);

                File xsl = (xslFile == null) ? ObservationManager.this.getXSLFile() : xslFile;

                // Get XSL Template
                if (xsl == null) { // Cannot load XSL file. Error message was
                                   // already given

                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.transformation");
                    return;

                }
                StreamSource xslSource = new StreamSource(xsl);

                StreamResult result = null;
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(htmlFile);
                    result = new StreamResult(outputStream);
                } catch (FileNotFoundException fnfe) {
                    System.err.println("Cannot transform XML file.\n" + fnfe);
                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.transformation");
                    return;
                }

                // Transform
                try {
                    Templates template = ObservationManager.this.getTemplate(xslSource); // Different loading
                                                                                         // between JDK1.4 and
                                                                                         // JDK1.5
                    if (template == null) {
                        returnValue = Worker.RETURN_TYPE_ERROR;
                        message = ObservationManager.bundle.getString("error.transformation");
                        try {
                            outputStream.close();
                        } catch (IOException ioe) {
                            System.err.println("Cannot close stream.\n" + ioe);
                        }
                        return;
                    }

                    template.newTransformer().transform(source, result);
                } catch (TransformerException tce) {
                    System.err.println("Cannot transform XML file.\n" + tce);
                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.transformation");
                    try {
                        outputStream.close();
                    } catch (IOException ioe) {
                        System.err.println("Cannot close stream.\n" + ioe);
                    }
                    return;
                }

                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    System.err.println("Cannot close stream.\n" + ioe);
                }

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        };

        // Show progresDialog for first part of export
        new ProgressDialog(this, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.html.info"), calculation);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }
            return true;
        } else {
            this.createWarning(calculation.getReturnMessage());
            return false;
        }

    }

    private File getXSLFile() {

        final String TEMPLATE_FILENAME = "transform";

        String selectedTemplate = this.configuration.getConfig(CONFIG_XSL_TEMPLATE);
        if ((selectedTemplate == null) // No config given, so take default one.
                                       // (Usefull for migrations)
                || ("".equals(selectedTemplate.trim()))) {
            selectedTemplate = "oal2html";
        }

        File path = new File(this.installDir.getPathForFolder("xsl") + selectedTemplate + File.separator);
        if (!path.exists()) {
            this.createWarning(ObservationManager.bundle.getString("warning.xslTemplate.dirDoesNotExist") + "\n"
                    + path.getAbsolutePath());
            return null;
        }

        // Try to load language dependend file first
        File xslFile = new File(path.getAbsolutePath() + File.separator + TEMPLATE_FILENAME + "_"
                + Locale.getDefault().getLanguage() + ".xsl");
        if (!xslFile.exists()) { // Ok, maybe theres a general version which is
                                 // not translated
            xslFile = new File(path.getAbsolutePath() + File.separator + TEMPLATE_FILENAME + ".xsl");
            if (!xslFile.exists()) {
                this.createWarning(ObservationManager.bundle.getString("warning.xslTemplate.noFileFoundWithName") + "\n"
                        + path.getAbsolutePath() + File.separator + TEMPLATE_FILENAME + ".xsl\n"
                        + path.getAbsolutePath() + File.separator + TEMPLATE_FILENAME + "_"
                        + Locale.getDefault().getLanguage() + ".xsl");
                return null;
            }
        }

        return xslFile;

    }

    private void createHTMLForSchemaElement(ISchemaElement schemaElement, File htmlFile) {

        // With that we can check whether there are observations at all.
        IObservation[] observations = this.xmlCache.getObservations(schemaElement);
        if ((observations == null) || (observations.length == 0)) {
            this.createWarning(ObservationManager.bundle.getString("error.export.xml.noObservationsForSchemaElement"));
            return;
        }

        // Get DOM source
        Document doc = this.xmlCache.getDocumentForSchemaElement(schemaElement);

        // XML File needs to be saved, as otherwise we don't get the path
        String[] files = this.xmlCache.getAllOpenedFiles();
        if ((files == null) || (files.length == 0)) { // There is data
                                                      // (otherwise we
                                                      // wouldn't have come
                                                      // here), but data's
                                                      // not saved
            this.createInfo(ObservationManager.bundle.getString("error.noXMLFileOpen"));
            return;
        }

        this.transformXML2HTML(doc, htmlFile, null);
        this.createInfo(ObservationManager.bundle.getString("info.htmlExportDir") + " " + htmlFile);

    }

    private void createXMLForSchemaElement(ISchemaElement schemaElement, File xmlFile) {

        /*
         * ProgressDialog progress = new ProgressDialog(this,
         * ObservationManager.bundle.getString("progress.wait.title"),
         * ObservationManager.bundle.getString("progress.wait.xml.info"));
         */
        // Create new XMLFileLoader for saving our new XML file
        XMLFileLoader xmlHelper = new XMLFileLoader(xmlFile);

        // Get all observations from currently opened XML that belong to the
        // given schemaElement
        IObservation[] observations = null;
        if (schemaElement instanceof IObservation) {
            observations = new IObservation[] { (IObservation) schemaElement };
        } else {
            observations = this.xmlCache.getObservations(schemaElement);
        }

        if ((observations == null) || (observations.length == 0)) {
            // progress.close();
            this.createWarning(ObservationManager.bundle.getString("error.export.xml.noObservationsForSchemaElement"));
            return;
        }

        // Add all observations and their depending elements to new
        // XMLFileLoader
        for (IObservation observation : observations) {
            xmlHelper.addSchemaElement(observation, true);
        }

        boolean result = xmlHelper.save(xmlFile.getAbsolutePath());

        // progress.close();

        if (result) {
            this.createInfo(ObservationManager.bundle.getString("error.export.xml.ok") + xmlFile);
        } else {
            this.createWarning(ObservationManager.bundle.getString("error.export.xml.nok"));
        }

    }

    private String getCurrentXMLParentPath() {

        // @todo
        // This whole method work only with one file opened!

        File xmlFile = new File(this.xmlCache.getAllOpenedFiles()[0]);

        return xmlFile.getParent();

    }

    private void enableMenus(boolean enabled) {

        for (int i = 0; i < this.menuBar.getMenuCount(); i++) {
            this.menuBar.getMenu(i).setEnabled(enabled);
        }

    }

    private void installExtension(File[] files) {

        // No files passed, so need to ask user for list of extensions
        if (files == null) {

            // Let user choose extension zip file
            JFileChooser chooser = new JFileChooser(ObservationManager.bundle.getString("extenstion.chooser.title"));
            FileFilter zipFileFilter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return (f.getName().endsWith(".ome")) || (f.isDirectory());
                }

                @Override
                public String getDescription() {
                    return "Observation Manager extensions";
                }
            };
            chooser.setFileFilter(zipFileFilter);
            String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
            if ((last != null) && !("".equals(last.trim()))) {
                File dir = new File(last);
                if (dir.exists()) {
                    chooser.setCurrentDirectory(dir);
                }
            }
            chooser.setMultiSelectionEnabled(true);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                files = chooser.getSelectedFiles();
            } else {
                return;
            }

        }

        // Check whether deployment can be done -> whether we've write
        // permissions for all files
        StringBuilder negativeResult = new StringBuilder();
        ArrayList filesOK = new ArrayList();
        try {
            boolean checkResult = false;
            for (File file : files) {
                checkResult = this.checkWriteAccess(new ZipFile(file), this.installDir.getInstallDir());
                if (!checkResult) {
                    negativeResult.append(" ").append(file.getName());
                } else {
                    filesOK.add(file);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Error while checking extension zip file. Zip file may be corrupted.\n" + ioe);
        }
        File[] filesCheckedOK = (File[]) filesOK.toArray(new File[] {});

        // --- Start with deployment

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        setCursor(hourglassCursor);

        StringBuilder positiveResult = new StringBuilder();
        int successCounter = 0;
        for (int i = 0; i < filesCheckedOK.length; i++) {
            try {
                positiveResult.append(" ").append(this.extLoader.addExtension(new ZipFile(filesCheckedOK[i])));
                successCounter++;
                if (i < filesCheckedOK.length - 1) { // There is at least one
                                                     // more ZIP to add
                    positiveResult.append(", ");
                }
            } catch (IOException ioe) {
                System.out.println("Error in extension zip file. Zip file may be corrupted.\n" + ioe);

                Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                setCursor(normalCursor);

                negativeResult.append(" ").append(filesCheckedOK[i].getName());
            }
        }

        boolean result = false;

        // Show all positive results
        if (successCounter > 0) {
            this.createInfo(ObservationManager.bundle.getString("info.addExtensionSuccess") + " " + positiveResult);

            // Until we found a better way to handle extension, we need to
            // restart... :-(
            if (true) {
                this.createInfo(ObservationManager.bundle.getString("info.addExtensionRestart"));
                // this.exit();
                this.menuFile.exit(this, this.changed);
            }

            result = true;
        }

        // Show all negative results
        if (successCounter < files.length) { // We check here against the
                                             // original files Array, to see
                                             // whether we had some
                                             // problems during check OR
                                             // installation
            this.createWarning(ObservationManager.bundle.getString("error.addExtensionFail") + " " + negativeResult);

            result = false;
        }

        // Inform about restart (if any installation was successfull)
        /*
         * if( successCounter > 0 ) { // Until we found a better way to handle
         * extension, we need to restart... :-( this.createInfo(ObservationManager
         * .bundle.getString("info.addExtensionRestart")); this.exit(); }
         */

        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(normalCursor);

    }

    private void enableNightVisionTheme(boolean enable) {

        if (enable) { // Turn on night vision theme

            try {
                // Check for Metal LAF
                LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
                boolean found = false;
                for (LookAndFeelInfo lookAndFeelInfo : laf) {
                    if ("metal".equals(lookAndFeelInfo.getName().toLowerCase())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.err.println(ObservationManager.bundle.getString("error.noMetalLAF"));
                    this.createWarning(ObservationManager.bundle.getString("error.noNightVision"));
                    return;
                }

                // Try to load MetalLookAndFeel
                MetalLookAndFeel.setCurrentTheme(new NightVisionTheme());
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                SwingUtilities.updateComponentTreeUI(this);

                // Make all frames and dialogs use the LookAndFeel

                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                this.dispose();
                this.setUndecorated(true);
                this.addNotify();
                this.createBufferStrategy(2);
                this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                this.update(super.getGraphics());
                this.configuration.setConfig(ObservationManager.CONFIG_NIGHTVISION_ENABLED, Boolean.toString(true));
                this.setVisible(true);

            } catch (Exception e) {
                System.err.println(e);
                this.createWarning(ObservationManager.bundle.getString("error.noNightVision"));
            }

        } else { // Turn off night vision theme
            try {

                // Try to load (default) OceanThema (available since Java 1.5)
                // with relfection
                Class themeClass = null;
                try {
                    themeClass = ClassLoader.getSystemClassLoader().loadClass("javax.swing.plaf.metal.OceanTheme");
                } catch (ClassNotFoundException cnfe) {
                    // Can do nothing in here...defaultMetalTheme will be
                    // loaded...
                }

                boolean problem = true;
                if (themeClass != null) { // Check if load OceanTheme succeeded
                    Constructor[] constructors = themeClass.getConstructors();
                    if (constructors.length > 0) {
                        Class[] parameters = null;
                        for (Constructor constructor : constructors) {
                            parameters = constructor.getParameterTypes();
                            if (parameters.length == 0) { // Use default
                                // constructor and
                                // set theme
                                MetalTheme theme = (MetalTheme) constructor.newInstance(null);
                                MetalLookAndFeel.setCurrentTheme(theme);
                                problem = false; // No problem -> no need to
                                // load DefaultMetalTheme as
                                // we can use OceanTheme
                                break;
                            }
                        }
                    }
                }

                if (problem) { // Ocean Theme cannot be used for whatever reason
                    MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                }

                UIManager.setLookAndFeel(new MetalLookAndFeel());
                SwingUtilities.updateComponentTreeUI(this);

                // Make all frames and dialogs use the LookAndFeel
                JFrame.setDefaultLookAndFeelDecorated(false);
                JDialog.setDefaultLookAndFeelDecorated(false);
                this.dispose();
                this.setUndecorated(false);
                this.addNotify();
                this.createBufferStrategy(2);
                this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
                this.update(super.getGraphics());
                this.configuration.setConfig(ObservationManager.CONFIG_NIGHTVISION_ENABLED, Boolean.toString(false));
                this.setVisible(true);

            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    private void importXML() {

        // Let user select the XML file
        JFileChooser chooser = new JFileChooser();
        FileFilter xmlFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".xml")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return "OAL Files";
            }
        };
        chooser.setFileFilter(xmlFileFilter);
        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) { // User canceled import
            return;
        }
        File file = chooser.getSelectedFile();

        // Check if a file was selected
        if (file == null) {
            return;
        }

        // Create and start the worker thread to do the actual import
        class ImportWorker implements Worker {

            private File importFile = null;
            private File schemaFile = null;
            private ObservationManager om = null;

            private String message = null;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            ImportWorker(File importFile, File schemaFile, ObservationManager om) {

                this.importFile = importFile;
                this.schemaFile = schemaFile;
                this.om = om;

            }

            @Override
            public void run() {

                // Load import file
                SchemaLoader importer = new SchemaLoader();
                try {
                    importer.load(importFile, schemaFile);
                } catch (OALException se) {
                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.import.xmlFile");
                    return;
                }

                // Get imported elements (without observations)
                ArrayList importedElements = new ArrayList();
                importedElements.addAll(Arrays.asList(importer.getEyepieces()));
                importedElements.addAll(Arrays.asList(importer.getFilters()));
                importedElements.addAll(Arrays.asList(importer.getImagers()));
                importedElements.addAll(Arrays.asList(importer.getObservers()));
                importedElements.addAll(Arrays.asList(importer.getScopes()));
                importedElements.addAll(Arrays.asList(importer.getSessions()));
                importedElements.addAll(Arrays.asList(importer.getSites()));
                importedElements.addAll(Arrays.asList(importer.getTargets()));
                importedElements.addAll(Arrays.asList(importer.getLenses()));

                // Add imported elements to current file
                for (Object importedElement : importedElements) {
                    this.om.getXmlCache().addSchemaElement((ISchemaElement) importedElement);
                }

                // Finally add observations
                // If we add the observations before all other elements are
                // known to the XMLLoader
                // the references are not set correctly, as only adding
                // observations, checks dependencies
                // and adds references in the XMLLoader.
                IObservation[] obs = importer.getObservations();
                if ((obs != null) && (obs.length > 0)) {
                    for (IObservation ob : obs) {
                        this.om.getXmlCache().addSchemaElement(ob);
                    }
                }

                // Refresh UI
                this.om.updateLeft(); // Refreshes tree (without that, the new
                                      // element won't appear on UI)

                // Set success message
                message = ObservationManager.bundle.getString("ok.import.xmlFile");

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        }

        ImportWorker calculation = new ImportWorker(file, this.schemaPath, this);

        // Change cursor, as import thread is about to start
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        setCursor(hourglassCursor);

        new ProgressDialog(this, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.xml.load.info"), calculation);

        // Change cursor back
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(normalCursor);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }

            // Make sure change flag is set
            this.setChanged(true);
        } else {
            this.createWarning(calculation.getReturnMessage());
        }

    }

    private void loadProjectFiles() {

        // Create an own thread that waits for the catalog loader
        // to finish. Only if all catalogs are loaded the project loader
        // might start in the background

        class WaitForCatalogLoader implements Runnable {

            private ObservationManager om = null;

            WaitForCatalogLoader(ObservationManager om) {

                this.om = om;

            }

            @Override
            public void run() {

                while (this.om.projectLoader == null) {
                    try {
                        if (!this.om.getExtensionLoader().getCatalogLoader().isLoading()) {
                            if (this.om.isDebug()) {
                                System.out.println("Catalog loading done. Start project loading in background...");
                            }
                            this.om.projectLoader = new ProjectLoader(this.om); // Initialite
                                                                                // ProjectLoader
                                                                                // and
                                                                                // start
                                                                                // loading
                                                                                // projects
                        } else {
                            this.wait(300);
                        }
                    } catch (InterruptedException ie) {
                        System.err.println("Interrupted while waiting for Catalog Loader to finish.\n" + ie);
                    } catch (IllegalMonitorStateException imse) {
                        // Ignore this
                    }
                }

            }

        }

        this.waitForCatalogLoaderThread = new Thread(new WaitForCatalogLoader(this),
                "Waiting for Catalog Loader to finish");
        waitForCatalogLoaderThread.start();

    }

    private void addShortcuts() {

        int menuKeyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // New Observation
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuKeyModifier), "NEW_OBSERVATION");
        this.getRootPane().getActionMap().put("NEW_OBSERVATION", new AbstractAction() {

            private static final long serialVersionUID = 54338866832362257L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ObservationManager.this.createNewObservation();
            }

        });

        // (Print) Show HTML export
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuKeyModifier), "PRINT");
        this.getRootPane().getActionMap().put("PRINT", new AbstractAction() {

            private static final long serialVersionUID = -5051798279720676416L;

            @Override
            public void actionPerformed(ActionEvent e) {

                ObservationManager.this.createHTML();

            }
        });

        // Help
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                "HELP");
        this.getRootPane().getActionMap().put("HELP", new AbstractAction() {

            private static final long serialVersionUID = 2672501453219731894L;

            @Override
            public void actionPerformed(ActionEvent e) {

                ObservationManager.this.showDidYouKnow();

            }
        });

        // Edit Observation
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, menuKeyModifier), "EDIT_OBSERVATION");
        this.getRootPane().getActionMap().put("EDIT_OBSERVATION", new AbstractAction() {

            private static final long serialVersionUID = 7853484982323650329L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ISchemaElement element = ObservationManager.this.getSelectedTableElement();
                if (element instanceof IObservation) {
                    // Edit current/selected observation
                    new ObservationDialog(ObservationManager.this, (IObservation) element);
                } else if (element instanceof ITarget) {
                    ITarget target = (ITarget) element;
                    ObservationManager.this.getExtensionLoader().getSchemaUILoader()
                            .getTargetDialog(target.getXSIType(), target, null);
                } else if (element instanceof IScope) {
                    new ScopeDialog(ObservationManager.this, (IScope) element);
                } else if (element instanceof IEyepiece) {
                    new EyepieceDialog(ObservationManager.this, (IEyepiece) element);
                } else if (element instanceof IImager) {
                    IImager imager = (IImager) element;
                    ObservationManager.this.getExtensionLoader().getSchemaUILoader()
                            .getSchemaElementDialog(imager.getXSIType(), SchemaElementConstants.IMAGER, imager, true);
                } else if (element instanceof ISite) {
                    new SiteDialog(ObservationManager.this, (ISite) element);
                } else if (element instanceof IFilter) {
                    new FilterDialog(ObservationManager.this, (IFilter) element);
                } else if (element instanceof ISession) {
                    new SessionDialog(ObservationManager.this, (ISession) element);
                } else if (element instanceof IObserver) {
                    new ObserverDialog(ObservationManager.this, (IObserver) element);
                } else if (element instanceof ILens) {
                    new LensDialog(ObservationManager.this, (ILens) element);
                }

            }

        });

        // Save file
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuKeyModifier), "SAVE_FILE");
        this.getRootPane().getActionMap().put("SAVE_FILE", new AbstractAction() {

            private static final long serialVersionUID = -4045748682943270961L;

            @Override
            public void actionPerformed(ActionEvent e) {

                ObservationManager.this.menuFile.saveFile(ObservationManager.this);
            }

        });

        // Open file
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuKeyModifier), "OPEN_FILE");
        this.getRootPane().getActionMap().put("OPEN_FILE", new AbstractAction() {

            private static final long serialVersionUID = -8299917980145286282L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ObservationManager.this.menuFile.openFile(ObservationManager.this, ObservationManager.this.changed);
            }

        });

    }

    public Map<String, String> getUIDataCache() {
        return uiDataCache;
    }


}

class TeeLog extends PrintStream {

    private PrintStream console = null;
    private final byte[] prefix;

    private static final Object syncMe = new Object();

    public TeeLog(PrintStream file) {

        this(file, "");

    }

    public TeeLog(PrintStream file, String prefix) {

        // Parent class writes to file
        super(file);

        // Prefix we set for every entry
        this.prefix = prefix.getBytes();

        // We write to console
        this.console = System.out;

    }

    @Override
    public void write(byte[] buf, int off, int len) {

        if ((buf == null) || (buf.length == 0)) {
            return;
        }

        String now = "  " + new Date().toString() + "\t";
        try {
            synchronized (TeeLog.syncMe) {
                if (!((buf[0] == (byte) 13) // (byte 13 -> carage return) So if
                                            // cr is send we do not put date &
                                            // prefix in
                                            // advance
                        || (buf[0] == (byte) 10)) // (byte 10 -> line feed) So if lf is
                                                  // send we do not put date & prefix
                                                  // in advance
                ) {
                    super.write(this.prefix, 0, this.prefix.length);
                    super.write(now.getBytes(), 0, now.length());
                }
                super.write(buf, off, len);

                if (!((buf[0] == (byte) 13) // (byte 13 -> carage return) So if
                                            // cr is send we do not put date &
                                            // prefix in
                                            // advance
                        || (buf[0] == (byte) 10)) // (byte 10 -> line feed) So if lf is
                                                  // send we do not put date & prefix
                                                  // in advance
                ) {
                    this.console.write(this.prefix, 0, this.prefix.length);
                    this.console.write(now.getBytes(), 0, now.length());
                }
                this.console.write(buf, off, len);
            }
        } catch (Exception e) {
            // Can't do anything in here
        }

    }

    @Override
    public void flush() {

        super.flush();
        synchronized (TeeLog.syncMe) {
            this.console.flush();
        }

    }

}

class NightVisionTheme extends DefaultMetalTheme {

    // Red shades
    private final ColorUIResource primary1 = new ColorUIResource(170, 30, 30); // Active
                                                                               // internal
                                                                               // window
                                                                               // borders
    private final ColorUIResource primary2 = new ColorUIResource(195, 34, 34); // Highlighting
                                                                               // to
                                                                               // indicate
                                                                               // activation
                                                                               // (for
                                                                               // example,
                                                                               // of
                                                                               // menu
                                                                               // titles
                                                                               // and
                                                                               // menu
                                                                               // items);
                                                                               // indication
                                                                               // of
                                                                               // keyboard
                                                                               // focus
    private final ColorUIResource primary3 = new ColorUIResource(255, 45, 45); // Large
                                                                               // colored
                                                                               // areas
                                                                               // (for
                                                                               // example,
                                                                               // the
                                                                               // active
                                                                               // title
                                                                               // bar)

    private final ColorUIResource secondary1 = new ColorUIResource(92, 50, 50);
    private final ColorUIResource secondary2 = new ColorUIResource(124, 68, 68); // Inactive
                                                                                 // internal
                                                                                 // window
                                                                                 // borders;
                                                                                 // dimmed
                                                                                 // button
                                                                                 // borders
    private final ColorUIResource secondary3 = new ColorUIResource(181, 99, 99); // Canvas
                                                                                 // color
                                                                                 // (that
                                                                                 // is,
                                                                                 // normal
                                                                                 // background
                                                                                 // color);
                                                                                 // inactive
                                                                                 // title
                                                                                 // bar

    private final ColorUIResource white = new ColorUIResource(255, 175, 175);

    @Override
    public String getName() {

        return "Night Vision";

    }

    // -----------------
    // DefaultMetalTheme ------------------------------------------------------
    // -----------------

    @Override
    protected ColorUIResource getPrimary1() {

        return primary1;

    }

    @Override
    protected ColorUIResource getPrimary2() {

        return primary2;

    }

    @Override
    protected ColorUIResource getPrimary3() {

        return primary3;

    }

    @Override
    protected ColorUIResource getSecondary1() {

        return secondary1;

    }

    @Override
    protected ColorUIResource getSecondary2() {

        return secondary2;

    }

    @Override
    protected ColorUIResource getSecondary3() {

        return secondary3;

    }

    @Override
    protected ColorUIResource getWhite() {

        return white;

    }

}
