package jp.opencollector.application.jpkipdf;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBundleGetter implements BundleGetter {
    public void addBundleChangeListener(BundleChangeListener l) {
        listeners.add(l);
    }

    public void removeBundleChangeListener(BundleChangeListener l) {
        listeners.remove(l);
    }

    protected void fireBundleChangeEvent() {
        BundleChangeEvent e = new BundleChangeEvent(this, getResourceBundle());
        for (BundleChangeListener l: listeners) {
            l.bundleChanged(e);
        }
    }

    protected Set<BundleChangeListener> listeners = new HashSet<BundleChangeListener>();
}