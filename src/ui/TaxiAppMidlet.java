package ui;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

import database.*;
import search_engine.*;

/**
 * @author Fuad
 */
public class TaxiAppMidlet extends MIDlet implements CommandListener, Runnable {
    private TaxiAppMidlet theApp;
    private static Display display;
    
    private Command NON_LOC_OK_CMD;
    
    private Command EXIT_CMD;
    private Command OK_CMD;
    private Command SETTING_CMD;
    
    private Command BACK_CMD;
    private Command NEXT_CMD;
    
    private Command SEARCH_CMD;
    private Command SELECT_CMD;
    
    private Command QUIT_SEARCH_CMD;
    private Command NEW_CMD;
    
    public UserSettings settings;
    public RoadMap roadMap;
    public LearningData ml_db;
    public Locale locale;
    
    LocationForm startPlaceForm, endPlaceForm;    
    SettingsForm setting_form;
    
    private boolean firstTime;
    private boolean error_occured;

    private String start_place, end_place;
    
    public TaxiAppMidlet() {
        theApp = TaxiAppMidlet.this;
        display = Display.getDisplay(theApp);
        firstTime = true;
        NON_LOC_OK_CMD = new Command("Ok|እሺ", Command.OK, 1);
    }
    
    private void initialize_components() {
        EXIT_CMD = new Command(locale.getString("Command.Exit"), Command.SCREEN, 3);
        OK_CMD = new Command(locale.getString("Command.OK"), Command.OK, 1);
        SETTING_CMD = new Command(locale.getString("Command.Settings"), Command.SCREEN, 1);
    
        BACK_CMD = new Command(locale.getString("Command.Back"), Command.BACK, 2);
        NEXT_CMD = new Command(locale.getString("Command.Next"), Command.OK, 1);
    
        SEARCH_CMD = new Command(locale.getString("Command.Search"), Command.SCREEN, 2);
        NEW_CMD = new Command(locale.getString("Command.New Search"), Command.SCREEN, 2);
        
        SELECT_CMD = new Command(locale.getString("Command.Select"), Command.OK, 1);
        QUIT_SEARCH_CMD = new Command(locale.getString("Command.Stop Search"), Command.CANCEL, 1);
        
        startPlaceForm = new LocationForm(locale.getString("Form.theApp.Start Location"), roadMap, new Command[]{EXIT_CMD, NEXT_CMD, SETTING_CMD});
        endPlaceForm = new LocationForm(locale.getString("Form.theApp.End Location"), roadMap, new Command[]{BACK_CMD, SEARCH_CMD});
        setting_form = null;
    }
    
    public void startApp() {
        if (firstTime) {
            firstTime = false;
            
            //int color = WelcomeScreen.Color.GREEN | WelcomeScreen.Color.RED;
            int color = 0;      // black
            
            display.setCurrent(new WelcomeScreen(color));
            loadResources();    // this will run in a separate thread
        }
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
        if (!error_occured) {
            roadMap.saveData();
            ml_db.saveData();
        }
        notifyDestroyed();
    }
    
    public synchronized void waitUntilFinished() {
        try {
            wait();
        } catch (InterruptedException e) { }
    }
    
    public synchronized void notifyFinished() {
        notifyAll();
    }
    
    public void loadResources() {
        new Thread(this).start();
    }
    
    /*
     * loads resources
     */
    public void run() {
        error_occured = true;   // App might be closed before we get to show our Welcome Screen.
        try {
            try {
                Thread.sleep(1500);     // wait for the splash to be seen
            } catch (InterruptedException e) { }
            settings = new UserSettings();
            if (settings.isFirstTime()) {
                FirstTimeLanguageSelector languageSelector;
                
                languageSelector = new FirstTimeLanguageSelector();
                display.setCurrent(languageSelector);
                
                waitUntilFinished();        // this will block until a language is selected
                if (languageSelector.selected == FirstTimeLanguageSelector.LANG_ENG)
                    settings.setLanguage(UserSettings.LANG_ENG);
                else
                    settings.setLanguage(UserSettings.LANG_AMH);
                settings.saveSettings();
            }
            locale = new Locale(settings);
            roadMap = new RoadMap(settings);
            ml_db = new LearningData();
            initialize_components();
            error_occured = false;
            display.setCurrent(startPlaceForm);
        } catch (Exception e) {
            error_occured = true;
            showErrorAndExit(e.getMessage());            
        }
    }

    public void showErrorAndExit(String err_msg) {
        Alert error_alert = new Alert("Error", err_msg, null, AlertType.ERROR);
        error_alert.addCommand(NON_LOC_OK_CMD);
        error_alert.setCommandListener(new CommandListener() {
            public void commandAction(Command cmd, Displayable disp) {
                theApp.destroyApp(true);
            }
        });
        display.setCurrent(error_alert);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (d == startPlaceForm) {
            if (c == NEXT_CMD) {
                start_place = startPlaceForm.getSelectedPlace();
                display.setCurrent(endPlaceForm);
            } else if (c == SETTING_CMD) {
                if (setting_form == null)
                    setting_form = new SettingsForm(startPlaceForm);
                display.setCurrent(setting_form);            
            } else if (c == EXIT_CMD) {
                notifyDestroyed();
            }
        } else if (d == endPlaceForm) {
            if (c == BACK_CMD) {
                display.setCurrent(startPlaceForm);
            } else if (c == SEARCH_CMD) {
                end_place = endPlaceForm.getSelectedPlace();
                if (end_place.equals(start_place) != true) {
                    Explorer searcher = new Explorer(roadMap, ml_db);
                    searcher.setEndPoints(roadMap.getNode(start_place), roadMap.getNode(end_place));
                    new SearchProgress(searcher, endPlaceForm, new ResultsForm(searcher, startPlaceForm));
                }
            }
            
        }
    }
    
    class FirstTimeLanguageSelector extends Form implements CommandListener {
        public static final int LANG_ENG = 0;
        public static final int LANG_AMH = 1;
        
        public int selected;
        
        private ChoiceGroup language;
        
        public FirstTimeLanguageSelector() {
            super("");
            append("Select Language\nቋንቋ ምረጥ");
            language = new ChoiceGroup("Languages(ቋንቋዎች):", Choice.EXCLUSIVE);
            language.append("English", null);
            language.append("አማርኛ", null);
            append(language);
            addCommand(NON_LOC_OK_CMD);
            setCommandListener(FirstTimeLanguageSelector.this);
        }
        
        public void commandAction(Command cmd, Displayable disp) {
            if (language.getSelectedIndex() == 0)
                selected = LANG_ENG;
            else
                selected = LANG_AMH;
            notifyFinished();
        }
    }
    
    class LocationForm extends Form implements ItemStateListener { 
        private ChoiceGroup RegionCg;
        private ChoiceGroup PlaceCg;
        
        private RoadMap roadMap;
        private Enumeration regions;
        private Hashtable placesMap;
        
        /*
         * Constructs the LocationForm that accepts the region and 
         * the place you want to go. The region is a broad place to
         * divide the city to sizable groups. The places are the 
         * Taxi stations located in each region.
         * @param title The title that will be displayed.
         * @param reg An array of strings representing all the regoins 
         *          of a city.
         * @param palces A map from a region to a list of places contained
         *          in that regoin.
         * @param cmds An Array of Commands that this Form will show,
         *          NOTE: it is the job of the parent to handle the commands.
         */
        public LocationForm(String title, RoadMap roadMap, Command[] cmds) {
            super(title);
            this.roadMap = roadMap;
            
            RegionCg = new ChoiceGroup(locale.getString("ChoiceGroup.Regions"), Choice.POPUP);
            PlaceCg = new ChoiceGroup(locale.getString("ChoiceGroup.Places"), Choice.POPUP);
            append(RegionCg);
            append(PlaceCg);
            
            for (int i = 0; i < cmds.length; i++) { addCommand(cmds[i]); }
            
            setElements(RegionCg, roadMap.getRegions()); // set defaults
            String r = (String)roadMap.getRegions().nextElement();  // take the first as default
            setElements(PlaceCg, roadMap.getNodesInRegion(r));
            
            setCommandListener(TaxiAppMidlet.this);
            setItemStateListener(LocationForm.this);
        }
        
        public void itemStateChanged(Item item) {
            if (item == RegionCg) {
                String r = RegionCg.getString(RegionCg.getSelectedIndex());
                setElements(PlaceCg, roadMap.getNodesInRegion(r));
            }            
        }
        
        private void setElements(ChoiceGroup cg, Enumeration elems) {
            cg.deleteAll();
            
            while (elems.hasMoreElements()) {
                String s = (String)elems.nextElement();
                cg.append(s, null);
            }
        }
        
        private String getSelectedPlace() {
            return PlaceCg.getString(PlaceCg.getSelectedIndex());
        }
    } // end LocationForm
    
    class SettingsForm extends Form implements CommandListener {
        Displayable home;
        ChoiceGroup languages;
        int selected;
        public SettingsForm(Form home) {
            super(locale.getString("Settings"));
            
            this.home = home;
            languages = new ChoiceGroup(locale.getString("Languages"), ChoiceGroup.EXCLUSIVE);
            languages.append("English", null);
            languages.append("አማርኛ", null);         // this is amharic
            
            selected = (settings.getLanguage().equals(UserSettings.LANG_ENG) ?
                    0 : 1);
            languages.setSelectedIndex(selected, true);
            append(languages);
            
            addCommand(OK_CMD);
            addCommand(BACK_CMD);
            setCommandListener(SettingsForm.this);            
        }
        
        public void commandAction(Command cmd, Displayable disp) {
            if (cmd == OK_CMD) {
                int index = languages.getSelectedIndex();
                if (index == 0) 
                    settings.setLanguage(UserSettings.LANG_ENG);
                else
                    settings.setLanguage(UserSettings.LANG_AMH);
                settings.saveSettings();
                if (selected != index) {
                    selected = index;
                    Alert savingAlert = new Alert(locale.getString("savingAlert.Title"), locale.getString("savingAlert.Body"), null, AlertType.INFO);
                    savingAlert.addCommand(OK_CMD);
                    display.setCurrent(savingAlert, home);
                } else {
                    display.setCurrent(home);
                }
            } else if (cmd == BACK_CMD) {
                display.setCurrent(home);
            }
        }
    }
    
    class SearchProgress extends Alert implements CommandListener, SearchListener {
        private Form nextForm;
        private Form parentForm;
        private Explorer searcher;
        
        public SearchProgress(Explorer searcher, Form parent, Form next) {
            super(locale.getString("Alert.Searching.Title"), "", null, AlertType.CONFIRMATION);
            nextForm = next;
            parentForm = parent;
            this.searcher = searcher;
            this.setTimeout(FOREVER);
            this.setIndicator(new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
            addCommand(QUIT_SEARCH_CMD);
            setCommandListener(SearchProgress.this);
            searcher.setSearchListener(SearchProgress.this);
            display.setCurrent(SearchProgress.this);
            
            searcher.startSearch();     // begins the thread to search
        }
        
        public void commandAction(Command cmd, Displayable disp) {
            if (disp == this && cmd == QUIT_SEARCH_CMD) {
                searcher.quitSearch();
                display.setCurrent(parentForm);
            }
        }
        
        /*
         * this is when the search indicator will give way to 
         * the results from. It will be called by the Explorer when it 
         * finishes searching.
         */
        public void notifySearchFinished() {
            ((ResultsForm)nextForm).updateContents();
            display.setCurrent(nextForm);
        }        
    } // end SearchProgress
    
    class ResultsForm extends Form implements CommandListener, ItemCommandListener {
        Display disp;

        private Form mainMenu;
        private Explorer searcher;
        
        private StringItem[] choices;
        private String[] choice_strings;
        private boolean[] visited;
        
        private PathDisplayer[] path_displayers;
        
        private Path[] search_results;
        
        public ResultsForm(Explorer searcher, Form mainMenu) {
            super(locale.getString("ResultsForm.Title"));
            append(locale.getString("ResultsForm.Search Results:"));
            this.searcher = searcher;
            this.mainMenu = mainMenu;
            addCommand(NEW_CMD);
            addCommand(EXIT_CMD);
            
            String[] str_choices = splitString("First Second Third Fourth Fifth Sixth Seventh Eighth Ninth Tenth Eleventh Twelveth", ' ');
            choice_strings = new String[str_choices.length];
            int num = 0;
            
            for (; num < choice_strings.length; num++) {
                String loc_str = locale.getString("ResultsForm." + str_choices[num]);
                if (loc_str == null) break;
                choice_strings[num] = loc_str;
            }
            //choice_strings = new String[]{locale.getString("ResultsForm.First"), locale.getString("ResultsForm.Second"), locale.getString("ResultsForm.Third")};
            
            visited = new boolean[num];
            for (int i = 0; i < visited.length; i++) { visited[i] = false; }
            choices = new StringItem[num];
            path_displayers = new PathDisplayer[num];
            
            addCommand(NEW_CMD);
            setCommandListener(ResultsForm.this);
        }
        
        private String[] splitString(String s, char delim) {
            Vector sub_strings = new Vector();
            int last = 0, index;
            while ((index = s.indexOf(delim, last)) != -1) {
                sub_strings.addElement(s.substring(last, index));
                last = index + 1;
            }
            
            sub_strings.addElement(s.substring(last));
            
            String[] result = new String[sub_strings.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = (String)sub_strings.elementAt(i);
            }
            return result;
        }
        
        public void updateContents() {
            search_results = searcher.getSearchResults();
            
            int i;
            for (i = 0; (i < choices.length && i < search_results.length); i++) {
                choices[i] = new StringItem(null, choice_strings[i], Item.BUTTON);
                choices[i].setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);
                choices[i].setDefaultCommand(SELECT_CMD);
                choices[i].setItemCommandListener(ResultsForm.this);
                append(choices[i]);
            }
            
            if (i == 0) {       // nothing found
                append("\n" + locale.getString("Form.PathDisplayer.NoResultFound"));
            }
        }

        /*
         * Called when this form receives Commands.
         */
        public void commandAction(Command cmd, Displayable disp) {
            if (cmd == NEW_CMD) {
                display.setCurrent(mainMenu);
            } else if (cmd == EXIT_CMD) {
                theApp.destroyApp(true);
            }
        }

        /*
         * Called when either of the choices is selected
         */
        public void commandAction(Command c, Item item) {
            for (int i = 0; i < choices.length; i++) {
                if (item.equals(choices[i])) {
                    if (!visited[i])
                        path_displayers[i] = new PathDisplayer(ResultsForm.this, mainMenu, search_results[i]);
                    visited[i] = true;
                    display.setCurrent(path_displayers[i]);
                }
            }
        }
    } // end ResultsForm    
    
    /*
     * This will show a path from start to finish.
     */
    class PathDisplayer extends Form implements CommandListener {
        Form parent;
        Form mainMenu;
        Path path;
        
        public PathDisplayer(Form parent, Form mainMenu, Path p) {
            super(locale.getString("Form.PathDisplayer.Title"));
            
            this.parent = parent;
            this.mainMenu = mainMenu;
            path = p;
            
            double val = p.getRank(p.getParameters());
            append(new StringItem(null, locale.getString("Form.PathDisplayer.Path.Rank") + " " + stringForm(val,2) + "\n")); 
            for (Enumeration nodes = path.getNodes(); nodes.hasMoreElements(); ) {
                Node node = (Node)nodes.nextElement();

                StringItem item = new StringItem("", node.getName() + "\n");
                //item.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_CENTER | Item.LAYOUT_EXPAND);
                append(item);
            }
            
            addCommand(BACK_CMD);
            setCommandListener(PathDisplayer.this);
        }
        
        private String stringForm(double d, int place) {
            String str = Double.toString(d);
            if (str.indexOf('.') != -1)
                place = place + 1 + str.indexOf('.');
            else
                place = str.length();
            return str.substring(0, place);
        }
        public void commandAction(Command cmd, Displayable disp) {
            if (cmd == BACK_CMD) {
                display.setCurrent(parent);
            }            
        }
    } // end PathDisplayer
} // end 
