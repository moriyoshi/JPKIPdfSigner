package jp.opencollector.application.jpkipdf;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class MonthComboBoxModel implements ComboBoxModel, BundleChangeListener {
    static final String[] months = {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };

    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    public Object getElementAt(int index) {
        return monthNames[index];
    }

    public int getSize() {
        return monthNames.length;
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    protected void fireListDataChanged() {
        for (ListDataListener l: listeners) {
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    public Object getSelectedItem() {
        return this.selectedItemIndex;
    }

    public void setSelectedItem(Object anItem) {
        if (anItem instanceof Number) {
            this.selectedItemIndex = ((Number) anItem).intValue();
            return;
        }
        int _selectedItemIndex = -1;
        for (int i = 0; i < monthNames.length; i++) {
            if (monthNames[i].equals(anItem))
                _selectedItemIndex = i;
        }
        this.selectedItemIndex = _selectedItemIndex;
    }

    private void populate(ResourceBundle b) {
        if (b == null) {
            monthNames = months;
            return;
        }
        String[] _monthNames = new String[12];
        for (int i = 0; i < months.length; i++) {
            final String month = months[i];
            String _monthName = b.getString(MonthComboBoxModel.class.getCanonicalName() + "." + month);
            _monthNames[i] = _monthName != null ? _monthName: month;
        }
        monthNames = _monthNames;
        fireListDataChanged();
    }

    public void bundleChanged(BundleChangeEvent e) {
        populate(e.getResourceBundle());
    }

    public MonthComboBoxModel(BundleGetter bundleGetter) {
        this.bundleGetter = bundleGetter;
        populate(bundleGetter.getResourceBundle());
    }

    Set<ListDataListener> listeners = new HashSet<ListDataListener>();
    int selectedItemIndex = -1;
    BundleGetter bundleGetter;
    String[] monthNames;
}
