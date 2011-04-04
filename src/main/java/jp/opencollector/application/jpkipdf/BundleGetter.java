package jp.opencollector.application.jpkipdf;

import java.util.ResourceBundle;

public interface BundleGetter {
    ResourceBundle getResourceBundle();
    void addBundleChangeListener(BundleChangeListener l);
    void removeBundleChangeListener(BundleChangeListener l);
}