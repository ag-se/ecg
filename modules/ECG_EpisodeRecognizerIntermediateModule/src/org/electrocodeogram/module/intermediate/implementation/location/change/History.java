package org.electrocodeogram.module.intermediate.implementation.location.change;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.electrocodeogram.module.intermediate.implementation.location.state.IText;
import org.electrocodeogram.module.intermediate.implementation.location.state.Location;
import org.electrocodeogram.module.intermediate.implementation.location.state.LocationComparator;
import org.electrocodeogram.module.intermediate.implementation.location.state.Text;

public class History {
    
    private final boolean STORE_HISTORY = false;

    private HashMap<IText, HashMap<Location, List<LocationChange>>> textLocationChangeHistories 
                = new HashMap<IText, HashMap<Location, List<LocationChange>>>();
    
    private List<BlockChange> blockChangeHistory = new ArrayList<BlockChange>();

    public List<BlockChange> getBlockChangeHistory() {
        return blockChangeHistory;
    }

    private HashMap<Location, List<LocationChange>> getLocationChangeHistories(IText text) {
        HashMap<Location, List<LocationChange>> locationChangeHistories = textLocationChangeHistories.get(text);
        if (locationChangeHistories == null) {
            locationChangeHistories = new LinkedHashMap<Location, List<LocationChange>>();
            this.textLocationChangeHistories.put(text, locationChangeHistories);
        }
        return locationChangeHistories;
    }

    /**
     * Adds locationChange to history. Will fetch latest contents, startline, and length 
     * from the location. Will also add the locationChange to the blockChange.
     * @param locationChange, with be ignored if null
     */
    public void addLocationChange(LocationChange lc, BlockChange bc) {
        if (!STORE_HISTORY) return;
        IText text = bc.getText();
        HashMap<Location, List<LocationChange>> locationChangeHistories = getLocationChangeHistories(text);
        if (lc == null)
            return;
        Location loc = lc.getLocation();
        if (lc.isAlive()) {
            lc.setContents(loc.getContents());
        }
        else
            lc.setContents("");            
        List<LocationChange> locationChanges = locationChangeHistories.get(loc);
        if (locationChanges == null) {
            locationChanges = new ArrayList<LocationChange>();
            locationChangeHistories.put(loc, locationChanges);
        }
        locationChanges.add(lc);
        if (bc != null)
            lc.setBlockChange(bc);
        if (lc.getBlockChange() != null)
            lc.getBlockChange().getLocationChanges().add(lc); // TODO this is a bit strange
    }
    
    /**
     * Adds blockChange to history. 
     * @param blockChange, with be ignored if null
     */
    public void addBlockChange(BlockChange bc) {
        if (!STORE_HISTORY) return;
        getBlockChangeHistory().add(bc);
    }

    public String printLocationChangeHistory(IText text) {
        HashMap<Location, List<LocationChange>> locationChangeHistories = getLocationChangeHistories(text);
        String res = "";
        SortedSet<Location> locs = new TreeSet<Location>(LocationComparator.getComparator());
        locs.addAll(locationChangeHistories.keySet());
        for (Location loc : locs) {
            res += "Location " + loc.getId() + ": \n";
            res += "**********************************************\n";
            Collection<LocationChange> lcs = locationChangeHistories.get(loc);
            for (LocationChange lc : lcs) {
                res += "---- " + lc.toString();
                res += " (since " + lc.getBlockChange().toString() + ")";
                res += " ----\n";
                if (lc.isAlive())
                    res += lc.getContents() + "\n";
                else
                    res += "LOCATION IS DEAD!\n\n";
            }
        }
        return res;
    }
    
    public String printBlockChangeHistory() {
        // java.util.Collections.sort(blockChangeHistory, BlockChangeComparator.getComparator());
        String res = "";
        for (BlockChange bc : blockChangeHistory) {
           res += "***************************** " + bc + "\n";
            for (LocationChange lc : bc.getLocationChanges()) {
                res += "  " + lc + "\n";
                res += lc.getContents() + "\n";
            }
        }
        return res;
    }
    
    public String printLastTextContents(Text text) {
        if (!STORE_HISTORY) return text.printContents();
        HashMap<Location, List<LocationChange>> locationChangeHistories = getLocationChangeHistories(text);
        String res = "";
        SortedSet<Location> locs = new TreeSet<Location>(LocationComparator.getComparator());
        locs.addAll(locationChangeHistories.keySet());
        for (Location loc : locs) {
            List<LocationChange> locChanges = locationChangeHistories.get(loc);
            if (locChanges != null && locChanges.get(locChanges.size()-1) != null) {
                LocationChange locChange = locChanges.get(locChanges.size()-1);
                if (locChange.isAlive())
                    res += locChange.getContents();
            }
        }
        return res;        
    }

    // Just for debugging
    public String printHistoryTextComparison(Text text) {
        HashMap<Location, List<LocationChange>> locationChangeHistories = getLocationChangeHistories(text);
        String res = "";
        SortedSet<Location> locs = new TreeSet<Location>(LocationComparator.getComparator());
        locs.addAll(locationChangeHistories.keySet());
        for (Location loc : locs) {
            res += loc + ":\n";
            List<LocationChange> locChanges = locationChangeHistories.get(loc);
            if (locChanges != null && locChanges.get(locChanges.size()-1) != null) {
                LocationChange locChange = locChanges.get(locChanges.size()-1);
                String locStr = loc.getContents();
                String lchStr = locChange.getContents();
                if (!text.containsLocation(loc))
                    res += "  --> not in latest text version\n";
                else
                    res += locStr;
                res += "  ----^-locStr-----" + (locStr.equals(lchStr) ? "equal" : "not equal") + "-----v-lchStr------\n";
                if (!locChange.isAlive())
                    res += "  --> not alive\n";
                else
                    res += lchStr;
            }
            else
                res += "no entry\n";
        }
        res += "****************************************************\n";
        return res;
    }
    
    public boolean checkValidity(Text text) {
        if (!STORE_HISTORY) return true;        
        // checkInit checks whether each LocationChange begins with an initial state
        boolean checkInit = true;
        // checkInner checks whether each inner LocationChange isnt a final state
        boolean checkInner = true;
        // checkRelated checks for each merge or split LocationChangeType whether a related location id is present
        boolean checkRelated = true;
        // checkLines checks for alive Locations that they have consequitive line ranges
        //   For this the initial sorting and the variable lineScan are necessary
        boolean checkLines = true;
        // checkInvalidLocs checks for each Location in non-Alive state if its not in text
        boolean checkInvalidLocs = true;
        // checkValidLocs checks for each Location from text whether its in the history
        boolean checkValidLocs = true;
        HashMap<Location, List<LocationChange>> locationChangeHistories = getLocationChangeHistories(text);
        Collection<Location> locs = locationChangeHistories.keySet();
        for (Location loc : locs) {
            List<LocationChange> locChs = locationChangeHistories.get(loc);
            for (int i = 0; i < locChs.size(); i++) {
                LocationChange locCh = locChs.get(i);
                if (i == 0) { // start of change history for this location
                    checkInit &= locCh.isFresh();
                    assert (checkInit);
                } else if (i < locChs.size() - 1) {
                    checkInner &= locCh.isAlive();
                    assert (checkInner);
                }
                if (locCh.isMerge() || locCh.isSplit()) {
                    checkRelated &= (locCh.getRelatedLocId() >= 0);
                    assert (checkRelated);
                }
                if (i == locChs.size() - 1) { // end of change history for this location
                    if (locCh.isAlive()) {
                        checkValidLocs &= text.containsLocation(loc);
                        assert (checkValidLocs);
                    } else {
                        checkInvalidLocs &= !text.containsLocation(loc);
                        assert (checkInvalidLocs);
                    }
                }
            }
        }
        for (Location loc : text.getLocations()) {
            checkValidLocs &= locs.contains(loc);
            assert (checkValidLocs);
        }
        return (checkInit & checkInner & checkRelated & checkLines & checkInvalidLocs & checkValidLocs);
    }
    
}
