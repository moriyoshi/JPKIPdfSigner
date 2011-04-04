package jp.opencollector.application.jpkipdf;

import java.util.EventObject;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
class BundleChangeEvent extends EventObject {
    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    public BundleChangeEvent(Object source, ResourceBundle bundle) {
        super(source);
        this.bundle = bundle;
    }

    private ResourceBundle bundle;
}