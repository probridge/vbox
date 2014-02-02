package com.probridge.vbox.vmm.wmi.utils;

/**
 * This class is built to give pieces of information about exceptions
 * occurred while performing actions to a remote service.
 *
 */
//TODO make it a little bit more "usable" build other more "precise" exceptions
public class VirtualServiceException extends Exception {

    public VirtualServiceException(Throwable cause, String mess) {
        super(mess, cause);
    }

    public VirtualServiceException(Throwable e) {
        super(e);
    }

    public VirtualServiceException(String string) {
        super(string);
    }
}
