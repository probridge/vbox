package com.probridge.vbox.vmm.wmi.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;


/**
 * A representation of winmngmt service to be able to access remote host registry
 */
public class MyIJIWinReg {

    //private static final Logger logger = Logger.getLogger(MyIJIWinReg.class);

    private final IJIWinReg registry;
    private final ArrayList<JIPolicyHandle> openPolicies = new ArrayList<JIPolicyHandle>();

    /**
     * Instantiate a new WinReg object to be able to access remote host registry
     * @param url the address of host to access
     * @param user a user with enough permission to be able to use winmgnt
     * @param pwd user's password
     * @throws UnknownHostException
     */
    public MyIJIWinReg(String url, String user, String pwd) throws UnknownHostException {
        IJIAuthInfo authInfo = new JIDefaultAuthInfoImpl(url, user, pwd);
        registry = JIWinRegFactory.getSingleTon().getWinreg(authInfo, url, true);
    }

    /**
     * To read a HKEY_CURRENT_USER.
     * @param context the key context ( for example "Volatile Environment" )
     * @param key name of the key ( for example LOGONSERVER )
     * @param bufferSize the size of the buffer that handles the registry key value
     * @return a string array containing the registry value. In case of multi size value,
     * returns 2 strings, for all remaining cases, only one string is returned
     * @throws JIException
     */
    public String[] readHKCU(String context, String key, int bufferSize) throws JIException {
        JIPolicyHandle policyHandle = registry.winreg_OpenHKCU();
        openPolicies.add(policyHandle);
        JIPolicyHandle policyHandle2 = registry.winreg_OpenKey(policyHandle, context,
                IJIWinReg.KEY_ALL_ACCESS);
        openPolicies.add(policyHandle2);
        Object[] tmpDir = registry.winreg_QueryValue(policyHandle2, key, bufferSize);
        StringBuffer result = new StringBuffer();
        if (tmpDir[0].equals(IJIWinReg.REG_MULTI_SZ)) {
            //logger.debug("Reading REG_MULTI_SZ key from registry");
            StringBuffer resultBis = new StringBuffer();
            byte[][] val = (byte[][]) tmpDir[1];
            for (int i = 0; i < val[0].length; i++) {
                result.append((char) val[0][i]);
            }
            for (int i = 0; i < val[1].length; i++) {
                resultBis.append((char) val[1][i]);
            }
            String[] resArray = new String[2];
            resArray[0] = result.toString();
            resArray[1] = resultBis.toString();
            return resArray;
        } else {
            //logger.debug("Reading non REG_MULTI_SZ key from registry");
            byte[] val = (byte[]) tmpDir[1];
            for (int i = 0; i < val.length; i++) {
                result.append((char) val[i]);
            }
            return new String[] { result.toString() };
        }
    }

    /**
     * To cloes the connection with the registry
     */
    void close() {
        for (JIPolicyHandle handle : openPolicies) {
            try {
                registry.winreg_CloseKey(handle);
            } catch (JIException e) {
               // logger.warn("An exception occured while closing JIPolicyHandle", e);
            }
        }
        try {
            registry.closeConnection();
        } catch (JIException e) {
           // logger.warn("An exception occured while closing IJIWinReg", e);
        }
    }
}
